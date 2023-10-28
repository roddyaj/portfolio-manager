package com.roddyaj.portfoliomanager.api.fidelity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.portfoliomanager.api.ParsingUtils;
import com.roddyaj.portfoliomanager.api.PortfolioReader;
import com.roddyaj.portfoliomanager.model.Order;
import com.roddyaj.portfoliomanager.model.Order.OrderType;
import com.roddyaj.portfoliomanager.model.Order.TransactionType;
import com.roddyaj.portfoliomanager.model.Portfolio;
import com.roddyaj.portfoliomanager.model.Position;

public class FidelityPortfolioReader implements PortfolioReader
{
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a v");

	@Override
	public Portfolio read(Path dir, String accountName, String accountNumber)
	{
		PositionInfo positionInfo = readPositions(dir);
		// @formatter:off
		return new Portfolio(
			positionInfo.positions(),
			readOpenOrders(dir),
			readTransactions(),
			positionInfo.cash(),
			positionInfo.balance(),
			positionInfo.time());
		// @formatter:on
	}

	private PositionInfo readPositions(Path dir)
	{
		Path file = ParsingUtils.getFile(dir, "Portfolio_Positions_.*\\.csv", (p1, p2) -> getTime(p2).compareTo(getTime(p1)));

		Map<Boolean, List<Position>> map = ParsingUtils.readCsv(file, 0).stream().filter(r -> r.size() > 1)
			.map(FidelityPortfolioReader::convertPosition).collect(Collectors.partitioningBy(p -> p.costBasis() == 0));
		List<Position> positions = map.get(false);
		List<Position> otherPositions = map.get(true);
		double cash = otherPositions.stream().mapToDouble(Position::getMarketValue).sum();
		double balance = positions.stream().mapToDouble(Position::getMarketValue).sum() + cash;
		return new PositionInfo(positions, cash, balance, getTime(file));
	}

	private static Position convertPosition(CSVRecord record)
	{
		Double price = ParsingUtils.parseDouble(record.get("Last Price"));

		// @formatter:off
		return new Position(
			record.get("Symbol"),
			ParsingUtils.parseString(record.get("Description")),
			price == null ? 1 : ParsingUtils.parseDouble(record.get("Quantity"), 0),
			price == null ? ParsingUtils.parseDouble(record.get("Current Value"), 0) : price,
			ParsingUtils.parseDouble(record.get("Today's Gain/Loss Percent"), 0),
			ParsingUtils.parseDouble(ParsingUtils.getOrNull(record, "Cost Basis Total"), 0),
			ParsingUtils.parseDouble(record.get("Percent Of Account"), 0),
			null
		);
		// @formatter:on
	}

	private static ZonedDateTime getTime(Path file)
	{
		ZonedDateTime time = null;
		try
		{
			List<String> lines = Files.readAllLines(file);

			Matcher matcher = Pattern.compile("Date downloaded (.+?)\"").matcher(lines.get(lines.size() - 1));
			if (matcher.find())
			{
				time = ZonedDateTime.parse(matcher.group(1), DATE_FORMAT);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return time;
	}

	private List<Order> readOpenOrders(Path dir)
	{
		List<Order> orders = new ArrayList<>();

		Path file = ParsingUtils.getFile(dir, "Activity Orders.*\\.htm",
			(p1, p2) -> ParsingUtils.getFileTime(p2).compareTo(ParsingUtils.getFileTime(p1)));
		Pattern pattern = Pattern.compile("(Buy|Sell) (.+?) Shares of (.+?) Limit at \\$(.+?) ");
		try
		{
			List<String> lines = Files.readAllLines(file);
			for (int i = 0; i < lines.size(); i++)
			{
				Matcher matcher = pattern.matcher(lines.get(i));
				if (matcher.find() && lines.get(i + 3).equals("Open"))
				{
					TransactionType transactionType = switch (matcher.group(1))
					{
						case "Buy" -> TransactionType.BUY;
						case "Sell" -> TransactionType.SELL;
						default -> null;
					};
					// @formatter:off
					orders.add(new Order(
						matcher.group(3),
						transactionType,
						Double.parseDouble(matcher.group(2)),
						Double.parseDouble(matcher.group(4)),
						OrderType.LIMIT,
						null,
						null));
					// @formatter:on
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return orders;
	}

	private List<Order> readTransactions()
	{
		return List.of();
	}

	private record PositionInfo(List<Position> positions, double cash, double balance, ZonedDateTime time) {};
}
