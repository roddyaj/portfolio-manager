package com.roddyaj.portfoliomanager.api.schwab2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.portfoliomanager.api.PositionsReader;
import com.roddyaj.portfoliomanager.model.InputPosition;
import com.roddyaj.portfoliomanager.model.InputPositions;
import com.roddyaj.schwabparse.SchwabOption;

public class SchwabPositionsReader implements PositionsReader
{
	private static final Pattern DATE_PATTERN = Pattern.compile("as of (.+?)\"");
	private static final DateTimeFormatter DATE_TIME_FORMAT_OLD = DateTimeFormatter.ofPattern("hh:mm a v, MM/dd/yyyy");
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a v, yyyy/MM/dd");

	private final Path dir;
	private final String accountName;

	public SchwabPositionsReader(Path dir, String accountName)
	{
		this.dir = dir;
		this.accountName = accountName;
	}

	@Override
	public Path getFile()
	{
		return getFile(dir, accountName + ".*-Positions-.*\\.csv",
			(p1, p2) -> SchwabPositionsReader.getTime(p2).compareTo(SchwabPositionsReader.getTime(p1)));
	}

	@Override
	public InputPositions read(Path file)
	{
		Map<Boolean, List<InputPosition>> map = Utils.readCsv(file, 2).stream().map(SchwabPositionsReader::convert)
			.collect(Collectors.partitioningBy(p -> p.quantity() == 0));
		List<InputPosition> positions = map.get(false);
		List<InputPosition> otherPositions = map.get(true);
		InputPosition balancePosition = otherPositions.stream().filter(p -> p.symbol().contains("Account Total")).findAny().orElse(null);
		double balance = balancePosition != null ? balancePosition.marketValue() : 0;
		InputPosition cashPosition = otherPositions.stream().filter(p -> p.symbol().contains("Cash")).findAny().orElse(null);
		double cash = cashPosition != null ? cashPosition.marketValue() : 0;
		return new InputPositions(positions, cash, balance, getTime(file));
	}

	public static ZonedDateTime getTime(Path file)
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

	private static InputPosition convert(CSVRecord record)
	{
		String symbol = record.get("Symbol");
		SchwabOption option = SchwabOption.parse(symbol);
		Double intrinsicValue = Utils.parseDouble(Utils.getOrNull(record, "Intrinsic Value"));

		// @formatter:off
		return new InputPosition(
			symbol,
			Utils.parseString(record.get("Description")),
			Utils.parseDouble(record.get("Quantity"), 0),
			Utils.parseDouble(record.get("Price"), 0),
			Utils.parseDouble(record.get("Market Value"), 0),
			Utils.parseDouble(record.get("Day Change %"), 0),
			Utils.parseDouble(Utils.getOrNull(record, "Cost Basis"), 0),
			Utils.parseDouble(record.get("% Of Account"), 0),
			option != null ? option.type() : null,
			option != null ? option.expiryDate() : null,
			option != null ? option.strike() : null,
			intrinsicValue,
			option != null && intrinsicValue != null ? (option.type().equals("P") ? option.strike() - intrinsicValue : option.strike() + intrinsicValue) : null,
			option != null ? option.symbol() : symbol,
			"ITM".equals(Utils.parseString(Utils.getOrNull(record, "In The Money")))
		);
		// @formatter:on
	}
}
