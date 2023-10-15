package com.roddyaj.portfoliomanager.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.roddyaj.portfoliomanager.model.InputPositions;

public interface PositionsReader
{
	Path getFile();

	InputPositions read(Path file);

	default Path getFile(Path dir, String pattern, Comparator<? super Path> comparator)
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
