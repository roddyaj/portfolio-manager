package com.roddyaj.portfoliomanager.api.fidelity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.portfoliomanager.api.PositionsReader;
import com.roddyaj.portfoliomanager.model.InputPosition;
import com.roddyaj.portfoliomanager.model.InputPositions;

public class FidelityPositionsReader implements PositionsReader
{
	private static final Pattern DATE_PATTERN = Pattern.compile("Date downloaded (.+?)\"");
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a v");

	private final Path dir;
	private final String accountName;

	public FidelityPositionsReader(Path dir, String accountName)
	{
		this.dir = dir;
		this.accountName = accountName;
	}

	@Override
	public Path getFile()
	{
		return getFile(dir, "Portfolio_Positions_.*\\.csv", (p1, p2) -> getTime(p2).compareTo(getTime(p1)));
	}

	@Override
	public InputPositions read(Path file)
	{
		Map<Boolean, List<InputPosition>> map = Utils.readCsv(file, 0).stream().filter(r -> r.size() > 1).map(FidelityPositionsReader::convert)
			.collect(Collectors.partitioningBy(p -> p.costBasis() == 0));
		List<InputPosition> positions = map.get(false);
		List<InputPosition> otherPositions = map.get(true);
		double cash = otherPositions.stream().mapToDouble(InputPosition::marketValue).sum();
		double balance = positions.stream().mapToDouble(InputPosition::marketValue).sum() + cash;
		return new InputPositions(positions, cash, balance, getTime(file));
	}

	public static ZonedDateTime getTime(Path file)
	{
		ZonedDateTime time = null;
		try
		{
			List<String> lines = Files.readAllLines(file);

			Matcher matcher = DATE_PATTERN.matcher(lines.get(lines.size() - 1));
			if (matcher.find())
			{
				time = ZonedDateTime.parse(matcher.group(1), DATE_TIME_FORMAT);
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
		// @formatter:off
		return new InputPosition(
			record.get("Symbol"),
			Utils.parseString(record.get("Description")),
			Utils.parseDouble(record.get("Quantity"), 0),
			Utils.parseDouble(record.get("Last Price"), 0),
			Utils.parseDouble(record.get("Current Value"), 0),
			Utils.parseDouble(record.get("Today's Gain/Loss Percent"), 0),
			Utils.parseDouble(Utils.getOrNull(record, "Cost Basis Total"), 0),
			Utils.parseDouble(record.get("Percent Of Account"), 0),
			null,
			null,
			null,
			null,
			null,
			record.get("Symbol"),
			false
		);
		// @formatter:on
	}
}
