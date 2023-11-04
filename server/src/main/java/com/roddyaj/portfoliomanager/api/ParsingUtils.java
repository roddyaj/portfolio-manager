package com.roddyaj.portfoliomanager.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public final class ParsingUtils
{
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");

	public static LocalDateTime getFileTime(Path file)
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

	public static Path getFile(Path dir, String pattern, Comparator<? super Path> comparator)
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

	public static Stream<Path> list(Path dir, String pattern)
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

	public static List<CSVRecord> readCsv(Path path, int headerLine)
	{
		if (path == null)
			return List.of();

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

	private ParsingUtils()
	{
	}
}
