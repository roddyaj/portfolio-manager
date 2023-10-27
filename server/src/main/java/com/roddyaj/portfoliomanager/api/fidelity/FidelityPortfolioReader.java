package com.roddyaj.portfoliomanager.api.fidelity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import com.roddyaj.portfoliomanager.model2.Order;
import com.roddyaj.portfoliomanager.model2.Portfolio;
import com.roddyaj.portfoliomanager.model2.Position;

public class FidelityPortfolioReader implements PortfolioReader
{
	@Override
	public Portfolio read(Path dir, String accountName, String accountNumber)
	{
		PositionInfo positionInfo = readPositions(dir);
		return new Portfolio(
			positionInfo.positions(),
			readOpenOrders(),
			readTransactions(),
			positionInfo.cash(),
			positionInfo.balance(),
			positionInfo.time());
	}

	// Positions

	private PositionInfo readPositions(Path dir)
	{
		Path file = getFile(dir, "Portfolio_Positions_.*\\.csv", (p1, p2) -> getTime(p2).compareTo(getTime(p1)));

		Map<Boolean, List<Position>> map = Utils.readCsv(file, 0).stream().filter(r -> r.size() > 1).map(FidelityPortfolioReader::convertPosition)
			.collect(Collectors.partitioningBy(p -> p.costBasis() == 0));
		List<Position> positions = map.get(false);
		List<Position> otherPositions = map.get(true);
		double cash = otherPositions.stream().mapToDouble(Position::getMarketValue).sum();
		double balance = positions.stream().mapToDouble(Position::getMarketValue).sum() + cash;
		return new PositionInfo(positions, cash, balance, getTime(file));
	}

	private record PositionInfo(List<Position> positions, double cash, double balance, ZonedDateTime time)
	{
	};

	private static Position convertPosition(CSVRecord record)
	{
		Double price = Utils.parseDouble(record.get("Last Price"));

		// @formatter:off
		return new Position(
			record.get("Symbol"),
			Utils.parseString(record.get("Description")),
			price == null ? 1 : Utils.parseDouble(record.get("Quantity"), 0),
			price == null ? Utils.parseDouble(record.get("Current Value"), 0) : price,
			Utils.parseDouble(record.get("Today's Gain/Loss Percent"), 0),
			Utils.parseDouble(Utils.getOrNull(record, "Cost Basis Total"), 0),
			Utils.parseDouble(record.get("Percent Of Account"), 0),
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
				time = ZonedDateTime.parse(matcher.group(1), DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a v"));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return time;
	}

	private List<Order> readOpenOrders()
	{
		return List.of();
	}

	private List<Order> readTransactions()
	{
		return List.of();
	}

	// Generic utilities (not specific to data provider or record type)

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
