package com.roddyaj.portfoliomanager.schwab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.roddyaj.portfoliomanager.model.State;

public abstract class AbstractMonitor
{
	private final Path dir;

	protected final String accountName;

	protected final String accountNumber;

	private final State state;

	private FileTime dirLastModified;

	private Path file;

	public AbstractMonitor(Path dir, String accountName, String accountNumber, State state)
	{
		this.dir = dir;
		this.accountName = accountName;
		this.accountNumber = accountNumber;
		this.state = state;
	}

	public boolean check()
	{
		boolean updated = false;
		try
		{
			FileTime lastModified = Files.getLastModifiedTime(dir);
			if (!lastModified.equals(dirLastModified))
			{
				dirLastModified = lastModified;

				Path file = getFile();
				if (file != null && !file.equals(this.file))
				{
					this.file = file;
					updated = true;
					updateState(file, state);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return updated;
	}

	protected abstract Path getFile();

	protected abstract void updateState(Path file, State state);

	protected Path getFile(String pattern, Comparator<? super Path> comparator)
	{
		Path file = null;
		List<Path> files = list(dir, pattern).sorted(comparator).toList();
		if (!files.isEmpty())
		{
			file = files.get(0);
//			for (int i = 1; i < files.size(); i++)
//			{
//				try
//				{
//					Files.delete(files.get(i));
//				}
//				catch (IOException e)
//				{
//					e.printStackTrace();
//				}
//			}
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
