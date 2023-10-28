package com.roddyaj.portfoliomanager.api.schwab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.portfoliomanager.api.ParsingUtils;
import com.roddyaj.portfoliomanager.model.Option;
import com.roddyaj.portfoliomanager.model.Position;

final class SchwabPositionsReader
{
	private static final Pattern DATE_PATTERN = Pattern.compile("as of (.+?)\"");
	private static final DateTimeFormatter DATE_TIME_FORMAT_OLD = DateTimeFormatter.ofPattern("hh:mm a v, MM/dd/yyyy");
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a v, yyyy/MM/dd");

	public static PositionInfo readPositions(Path dir, String accountName)
	{
		String pattern = accountName + ".*-Positions-.*\\.csv";
		Comparator<? super Path> comparator = (p1, p2) -> getTime(p2).compareTo(getTime(p1));
		Path file = ParsingUtils.getFile(dir, pattern, comparator);

		List<String> specialSymbols = List.of("Cash & Cash Investments", "Account Total");
		Map<Boolean, List<Position>> map = ParsingUtils.readCsv(file, 2).stream().map(SchwabPositionsReader::convertPosition)
			.collect(Collectors.partitioningBy(p -> specialSymbols.contains(p.symbol())));
		List<Position> positions = map.get(false);
		List<Position> otherPositions = map.get(true);
		Position balancePosition = otherPositions.stream().filter(p -> "Account Total".equals(p.symbol())).findAny().orElse(null);
		double balance = balancePosition != null ? balancePosition.getMarketValue() : 0;
		Position cashPosition = otherPositions.stream().filter(p -> "Cash & Cash Investments".equals(p.symbol())).findAny().orElse(null);
		double cash = cashPosition != null ? cashPosition.getMarketValue() : 0;
		return new PositionInfo(positions, cash, balance, getTime(file));
	}

	private static Position convertPosition(CSVRecord record)
	{
		String symbol = record.get("Symbol");
		Double price = ParsingUtils.parseDouble(record.get("Price"));
		Option option = SchwabUtils.convertOption(symbol, ParsingUtils.parseDouble(ParsingUtils.getOrNull(record, "Intrinsic Value"), 0),
			"ITM".equals(ParsingUtils.parseString(ParsingUtils.getOrNull(record, "In The Money"))));
		if (option != null)
		{
			symbol = option.symbol();
		}

		// @formatter:off
		return new Position(
			symbol,
			ParsingUtils.parseString(record.get("Description")),
			price == null ? 1 : ParsingUtils.parseDouble(record.get("Quantity"), 0),
			price == null ? ParsingUtils.parseDouble(record.get("Market Value"), 0) : price,
			ParsingUtils.parseDouble(record.get("Day Change %"), 0),
			ParsingUtils.parseDouble(ParsingUtils.getOrNull(record, "Cost Basis"), 0),
			ParsingUtils.parseDouble(record.get("% Of Account"), 0),
			option
		);
		// @formatter:on
	}

	private static ZonedDateTime getTime(Path file)
	{
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

	private SchwabPositionsReader()
	{
	}

	public record PositionInfo(List<Position> positions, double cash, double balance, ZonedDateTime time) {};
}
