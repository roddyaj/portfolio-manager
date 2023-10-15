package com.roddyaj.portfoliomanager.api.fidelity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

final class Utils
{
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");

	public static List<CSVRecord> readCsv(Path path, int headerLine)
	{
		List<String> lines;
		try
		{
			lines = Files.readAllLines(path);
			if (headerLine > 0)
				lines = lines.subList(headerLine, lines.size());
		}
		catch (IOException e)
		{
			lines = List.of();
			e.printStackTrace();
		}
		return readCsv(lines);
	}

	public static List<CSVRecord> readCsv(Collection<? extends String> lines)
	{
		List<CSVRecord> records = new ArrayList<>();

		String content = lines.stream().collect(Collectors.joining("\n"));
		CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setAllowMissingColumnNames(true).build();

		try
		{
			try (CSVParser parser = CSVParser.parse(content, format))
			{
				for (CSVRecord record : parser)
					records.add(record);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return records;
	}

	public static boolean parseBoolean(String value)
	{
		return "Yes".equals(value);
	}

	public static Integer parseInt(String value)
	{
		Integer intValue = null;
		if (isPresent(value))
		{
			try
			{
				intValue = Integer.parseInt(sanitize(value));
			}
			catch (NumberFormatException e)
			{
				System.out.println("parseInt " + e);
			}
		}
		return intValue;
	}

	public static Long parseLong(String value)
	{
		Long longValue = null;
		if (isPresent(value))
		{
			try
			{
				longValue = Long.parseLong(sanitize(value));
			}
			catch (NumberFormatException e)
			{
				System.out.println("parseLong " + e);
			}
		}
		return longValue;
	}

	public static Double parseDouble(String value)
	{
		Double doubleValue = null;
		if (isPresent(value))
		{
			try
			{
				doubleValue = Double.parseDouble(sanitize(value));
			}
			catch (NumberFormatException e)
			{
				System.out.println("parseDouble " + e);
			}
		}
		return doubleValue;
	}

	public static double parseDouble(String value, double defaultValue)
	{
		Double parsedValue = parseDouble(value);
		return parsedValue != null ? parsedValue.doubleValue() : defaultValue;
	}

	public static String parseString(String value)
	{
		return isPresent(value) ? value : null;
	}

	public static LocalDate parseDate(String value)
	{
		LocalDate date = null;
		if (isPresent(value) && value.contains("/"))
		{
			try
			{
				date = LocalDate.parse(value, DATE_FORMAT);
			}
			catch (DateTimeParseException e)
			{
				date = LocalDate.parse(value.split(" ")[0], DATE_FORMAT);
			}
		}
		return date;
	}

	public static String getOrNull(CSVRecord record, String fieldName)
	{
		return record.isSet(fieldName) ? record.get(fieldName) : null;
	}

	private static boolean isPresent(String value)
	{
		return !(value == null || value.isBlank() || "--".equals(value) || "N/A".equalsIgnoreCase(value));
	}

	private static String sanitize(String value)
	{
		return value.replace(",", "").replace("$", "").replace("%", "");
	}

	private Utils()
	{
	}
}
