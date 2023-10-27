package com.roddyaj.portfoliomanager.api.schwab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.portfoliomanager.api.PortfolioReader;
import com.roddyaj.portfoliomanager.api.schwab2.Utils;
import com.roddyaj.portfoliomanager.model2.Option;
import com.roddyaj.portfoliomanager.model2.Order;
import com.roddyaj.portfoliomanager.model2.Order.OrderType;
import com.roddyaj.portfoliomanager.model2.Order.TransactionType;
import com.roddyaj.portfoliomanager.model2.Portfolio;
import com.roddyaj.portfoliomanager.model2.Position;

public class SchwabPortfolioReader implements PortfolioReader
{
	@Override
	public Portfolio read(Path dir, String accountName, String accountNumber)
	{
		PositionInfo positionInfo = readPositions(dir, accountName);
		return new Portfolio(
			positionInfo.positions(),
			readOpenOrders(dir, accountName, accountNumber),
			readTransactions(dir, accountName, accountNumber),
			positionInfo.cash(),
			positionInfo.balance(),
			positionInfo.time());
	}

	// Positions

	private PositionInfo readPositions(Path dir, String accountName)
	{
		String pattern = accountName + ".*-Positions-.*\\.csv";
		Comparator<? super Path> comparator = (p1, p2) -> getPositionsTime(p2).compareTo(getPositionsTime(p1));
		Path file = getFile(dir, pattern, comparator);

		List<String> specialSymbols = List.of("Cash & Cash Investments", "Account Total");
		Map<Boolean, List<Position>> map = Utils.readCsv(file, 2).stream().map(SchwabPortfolioReader::convertPosition)
			.collect(Collectors.partitioningBy(p -> specialSymbols.contains(p.symbol())));
		List<Position> positions = map.get(false);
		List<Position> otherPositions = map.get(true);
		Position balancePosition = otherPositions.stream().filter(p -> "Account Total".equals(p.symbol())).findAny().orElse(null);
		double balance = balancePosition != null ? balancePosition.getMarketValue() : 0;
		Position cashPosition = otherPositions.stream().filter(p -> "Cash & Cash Investments".equals(p.symbol())).findAny().orElse(null);
		double cash = cashPosition != null ? cashPosition.getMarketValue() : 0;
		return new PositionInfo(positions, cash, balance, getPositionsTime(file));
	}

	private record PositionInfo(List<Position> positions, double cash, double balance, ZonedDateTime time) {};

	private static ZonedDateTime getPositionsTime(Path file)
	{
		final Pattern DATE_PATTERN = Pattern.compile("as of (.+?)\"");
		final DateTimeFormatter DATE_TIME_FORMAT_OLD = DateTimeFormatter.ofPattern("hh:mm a v, MM/dd/yyyy");
		final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a v, yyyy/MM/dd");

		ZonedDateTime time = null;
		try
		{
			List<String> lines = Files.readAllLines(file);

			Matcher matcher = DATE_PATTERN.matcher(lines.get(0));
			if (matcher.find())
			{
				try
				{
					time = ZonedDateTime.parse(matcher.group(1), DATE_TIME_FORMAT_OLD);
				}
				catch (DateTimeParseException e)
				{
					time = ZonedDateTime.parse(matcher.group(1), DATE_TIME_FORMAT);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return time;
	}

	private static Position convertPosition(CSVRecord record)
	{
		String symbol = record.get("Symbol");
		Double price = Utils.parseDouble(record.get("Price"));
		Option option = convertOption(symbol,
			Utils.parseDouble(Utils.getOrNull(record, "Intrinsic Value"), 0),
			"ITM".equals(Utils.parseString(Utils.getOrNull(record, "In The Money"))));
		if (option != null) {
			symbol = option.symbol();
		}

		// @formatter:off
		return new Position(
			symbol,
			Utils.parseString(record.get("Description")),
			price == null ? 1 : Utils.parseDouble(record.get("Quantity"), 0),
			price == null ? Utils.parseDouble(record.get("Market Value"), 0) : price,
			Utils.parseDouble(record.get("Day Change %"), 0),
			Utils.parseDouble(Utils.getOrNull(record, "Cost Basis"), 0),
			Utils.parseDouble(record.get("% Of Account"), 0),
			option
		);
		// @formatter:on
	}

	// Open orders

	private List<Order> readOpenOrders(Path dir, String accountName, String accountNumber)
	{
		String pattern = accountName.replace('_', ' ') + "XXXX" + accountNumber.substring(4) + "_Order_Status_.*\\.csv";
		Comparator<Path> comparator = (p1, p2) -> getFileTime(p2).compareTo(getFileTime(p1));
		Path file = getFile(dir, pattern, comparator);
		List<Order> orders = Utils.readCsv(file, 0).stream().map(SchwabPortfolioReader::convertOpenOrder).toList();
		return orders;
	}

	private static Order convertOpenOrder(CSVRecord record)
	{
		String symbol = Utils.parseString(record.get("Symbol"));
		Option option = convertOption(symbol, 0, false);
		if (option != null) {
			symbol = option.symbol();
		}

		// @formatter:off
		return new Order(
			symbol,
			convertTransactionType(record.get("Action")),
			Double.parseDouble(record.get("Quantity|Face Value").split(" ")[0]),
			getLimitPrice(record.get("Price")),
			convertOrderType(record.get("Price")),
			null,
			option
		);
		// @formatter:on
	}

	private static TransactionType convertTransactionType(String action)
	{
		return switch (action)
		{
			case "Buy" -> TransactionType.BUY;
			case "Sell" -> TransactionType.SELL;
			case "Sell to Open" -> TransactionType.SELL_TO_OPEN;
			case "Buy to Close" -> TransactionType.BUY_TO_CLOSE;
			case "Cash Dividend", "Qualified Dividend" -> TransactionType.DIVIDEND;
			case "Journal", "MoneyLink Deposit", "MoneyLink Transfer", "Funds Received", "Bank Transfer" -> TransactionType.TRANSFER;
			default -> null;
		};
	}

	private static OrderType convertOrderType(String price)
	{
		String orderType = price.contains(" ") ? price.split(" ")[0] : price;
		return switch (orderType)
		{
			case "Market" -> OrderType.MARKET;
			case "Limit" -> OrderType.LIMIT;
			default -> null;
		};
	}

	private static double getLimitPrice(String price)
	{
		return price.contains(" ") ? Utils.parseDouble(price.split(" ")[1]) : 0;
	}

	// Transactions

	private List<Order> readTransactions(Path dir, String accountName, String accountNumber)
	{
		String pattern = ".*_Transactions_.*\\.csv";
		Comparator<Path> comparator = (p1, p2) -> getTransactionsTime(p2).compareTo(getTransactionsTime(p1));
		Path file = getFile(dir, accountName + pattern, comparator);
		if (file == null)
		{
			String masked = "XXXXX" + accountNumber.substring(5);
			file = getFile(dir, masked + pattern, comparator);
		}
		List<Order> transactions = Utils.readCsv(file, 0).stream().map(SchwabPortfolioReader::convertTransaction).filter(t -> t.date() != null).toList();
		return transactions;
	}

	private static Order convertTransaction(CSVRecord record)
	{
		String symbol = Utils.parseString(record.get("Symbol"));
		double quantity = Utils.parseDouble(record.get("Quantity"), 0);
		Double price = Utils.parseDouble(record.get("Price"));
		double amount = Utils.parseDouble(record.get("Amount"), 0);
		Option option = convertOption(symbol, 0, false);
		if (option != null) {
			symbol = option.symbol();
		}

		// @formatter:off
		return new Order(
			symbol,
			convertTransactionType(record.get("Action")),
			price == null ? 1 : quantity,
			price == null ? amount : (option != null ? amount / quantity : price),
			null,
			Utils.parseDate(record.get("Date")),
			option
		);
		// @formatter:on
	}

	private static LocalDateTime getTransactionsTime(Path file)
	{
		LocalDateTime time = null;
		Matcher m = Pattern.compile("(.+?)_Transactions_([-\\d]+).csv").matcher(file.getFileName().toString());
		if (m.find())
			time = LocalDateTime.parse(m.group(2), DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
		return time;
	}

	// Schwab utilities

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");

	private static Option convertOption(String text, double intrinsicValue, boolean inTheMoney)
	{
		if (!isOption(text))
			return null;

		String[] tokens = text.split(" ");
		String symbol = tokens[0];
		LocalDate expiryDate = LocalDate.parse(tokens[1], DATE_FORMAT);
		double strike = Double.parseDouble(tokens[2]);
		Option.Type type = switch (tokens[3]) {
			case "C" -> Option.Type.CALL;
			case "P" -> Option.Type.PUT;
			default -> null;
		};
		return new Option(symbol, expiryDate, strike, type, intrinsicValue, inTheMoney);
	}

	private static boolean isOption(String symbol)
	{
		return symbol != null && symbol.indexOf(' ') != -1 && (symbol.endsWith("C") || symbol.endsWith("P"));
	}

	// Generic utilities (not specific to data provider or record type)

	private static LocalDateTime getFileTime(Path file)
	{
		LocalDateTime time = null;
		try
		{
			time = LocalDateTime.ofInstant(Files.getLastModifiedTime(file).toInstant(), ZoneId.systemDefault());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return time;
	}

	private static Path getFile(Path dir, String pattern, Comparator<? super Path> comparator)
	{
		Path file = null;
		List<Path> files = list(dir, pattern).sorted(comparator).toList();
		if (!files.isEmpty())
		{
			file = files.get(0);
			for (int i = 1; i < files.size(); i++)
			{
				try
				{
					Files.delete(files.get(i));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return file;
	}

	private static Stream<Path> list(Path dir, String pattern)
	{
		if (Files.exists(dir))
		{
			try
			{
				return Files.list(dir).filter(p -> p.getFileName().toString().matches(pattern));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return Stream.empty();
	}
}
