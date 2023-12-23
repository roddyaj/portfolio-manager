package com.roddyaj.portfoliomanager.api.schwab;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.portfoliomanager.api.ParsingUtils;
import com.roddyaj.portfoliomanager.model.Option;
import com.roddyaj.portfoliomanager.model.Order;

final class SchwabTransactionsReader
{
	private static final Pattern FILE_PATTERN = Pattern.compile("(.+?)_Transactions_([-\\d]+).csv");
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	public static List<Order> readTransactions(Path dir, String accountName, String accountNumber)
	{
		String pattern = "XXXXX" + accountNumber.substring(5) + "_Transactions_.*\\.csv";
		Comparator<Path> comparator = (p1, p2) -> getTime(p2).compareTo(getTime(p1));
		Path file = ParsingUtils.getFile(dir, pattern, comparator);
		return ParsingUtils.readCsv(file, 0).stream().map(SchwabTransactionsReader::convertTransaction).filter(t -> t.date() != null).toList();
	}

	private static Order convertTransaction(CSVRecord record)
	{
		String symbol = ParsingUtils.parseString(record.get("Symbol"));
		double quantity = ParsingUtils.parseDouble(record.get("Quantity"), 0);
		Double price = ParsingUtils.parseDouble(record.get("Price"));
		double amount = ParsingUtils.parseDouble(record.get("Amount"), 0);
		Option option = SchwabUtils.convertOption(symbol, 0, false);
		if (option != null)
		{
			symbol = option.symbol();
		}

		// @formatter:off
		return new Order(
			symbol,
			SchwabUtils.convertTransactionType(record.get("Action")),
			price == null ? 1 : quantity,
			price == null ? amount : (option != null ? amount / quantity : price),
			null,
			ParsingUtils.parseDate(record.get("Date")),
			option
		);
		// @formatter:on
	}

	private static LocalDateTime getTime(Path file)
	{
		LocalDateTime time = null;
		Matcher m = FILE_PATTERN.matcher(file.getFileName().toString());
		if (m.find())
			time = LocalDateTime.parse(m.group(2), DATE_FORMAT);
		return time;
	}

	private SchwabTransactionsReader()
	{
	}
}
