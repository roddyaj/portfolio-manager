package com.roddyaj.portfoliomanager.logic;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.roddyaj.portfoliomanager.api.ParsingUtils;
import com.roddyaj.portfoliomanager.model.Option;
import com.roddyaj.portfoliomanager.model.Option.Type;
import com.roddyaj.portfoliomanager.model.Order;
import com.roddyaj.portfoliomanager.model.Order.TransactionType;

/**
 * Persists transactions per account to ~/.invest/transactions so a full history survives the shorter lookback of the
 * brokerage downloads in ~/Downloads.
 */
public final class TransactionHistoryManager
{
	private static final Path HISTORY_DIR = Paths.get(System.getProperty("user.home"), ".invest", "transactions");

	private static final String[] HEADERS = { "date", "symbol", "transactionType", "quantity", "price", "optionSymbol", "optionExpiry",
		"optionStrike", "optionType" };

	/**
	 * Merges freshly downloaded transactions with the persisted history for the account, writing any newly seen past
	 * days to history, and returns the full combined list (history + today's downloaded transactions).
	 */
	public static List<Order> merge(String accountName, List<Order> downloaded)
	{
		Path file = HISTORY_DIR.resolve(accountName + ".csv");
		LocalDate today = LocalDate.now();

		List<Order> history = readHistory(file).stream().filter(t -> !today.equals(t.date())).toList();

		Set<LocalDate> historyDates = new HashSet<>();
		for (Order transaction : history)
			historyDates.add(transaction.date());

		List<Order> undated = downloaded.stream().filter(t -> t.date() == null).toList();
		List<Order> newHistory = downloaded.stream().filter(t -> t.date() != null && !today.equals(t.date()) && !historyDates.contains(t.date()))
			.sorted(Comparator.comparing(Order::date)).toList();
		List<Order> todayTransactions = downloaded.stream().filter(t -> today.equals(t.date())).toList();

		if (!newHistory.isEmpty())
			appendHistory(file, newHistory);

		List<Order> merged = new ArrayList<>(history.size() + newHistory.size() + todayTransactions.size() + undated.size());
		merged.addAll(history);
		merged.addAll(newHistory);
		merged.addAll(todayTransactions);
		merged.addAll(undated);
		merged.sort(Comparator.comparing(Order::date, Comparator.nullsFirst(Comparator.reverseOrder())));

		return merged;
	}

	private static List<Order> readHistory(Path file)
	{
		if (!Files.exists(file))
			return List.of();
		return ParsingUtils.readCsv(file, 0).stream().map(TransactionHistoryManager::convertRecord).toList();
	}

	private static Order convertRecord(CSVRecord record)
	{
		String optionSymbol = ParsingUtils.parseString(ParsingUtils.getOrNull(record, "optionSymbol"));
		Option option = optionSymbol == null ? null
			: new Option(optionSymbol, LocalDate.parse(record.get("optionExpiry")), Double.parseDouble(record.get("optionStrike")),
				Type.valueOf(record.get("optionType")), 0, false);

		String transactionType = ParsingUtils.parseString(record.get("transactionType"));

		// @formatter:off
		return new Order(
			ParsingUtils.parseString(record.get("symbol")),
			transactionType == null ? null : TransactionType.valueOf(transactionType),
			Double.parseDouble(record.get("quantity")),
			Double.parseDouble(record.get("price")),
			null,
			LocalDate.parse(record.get("date")),
			option);
		// @formatter:on
	}

	private static void appendHistory(Path file, List<Order> transactions)
	{
		try
		{
			boolean isNewFile = !Files.exists(file);
			Files.createDirectories(file.getParent());

			CSVFormat format = isNewFile ? CSVFormat.DEFAULT.builder().setHeader(HEADERS).build() : CSVFormat.DEFAULT;
			StandardOpenOption[] options = isNewFile ? new StandardOpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.WRITE }
				: new StandardOpenOption[] { StandardOpenOption.APPEND };

			try (Writer writer = Files.newBufferedWriter(file, options); CSVPrinter printer = new CSVPrinter(writer, format))
			{
				for (Order t : transactions)
				{
					Option option = t.option();
					printer.printRecord(t.date(), t.symbol(), t.transactionType(), t.quantity(), t.price(), option != null ? option.symbol() : null,
						option != null ? option.expiryDate() : null, option != null ? option.strike() : null, option != null ? option.type() : null);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private TransactionHistoryManager()
	{
	}
}
