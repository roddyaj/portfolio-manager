package com.roddyaj.portfoliomanager.schwab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractMonitor
{
	private final Path dir;

	protected final String accountName;

	protected final String accountNumber;

	private FileTime dirLastModified;

	private Path accountFile;

	public AbstractMonitor(Path dir, String accountName, String accountNumber)
	{
		this.dir = dir;
		this.accountName = accountName;
		this.accountNumber = accountNumber;
	}

	public Path check()
	{
		boolean updated = false;
		try
		{
			FileTime lastModified = Files.getLastModifiedTime(dir);
			if (!lastModified.equals(dirLastModified))
			{
				dirLastModified = lastModified;

				Path accountFile = getAccountFile();
				if (accountFile != null && !accountFile.equals(this.accountFile))
				{
					this.accountFile = accountFile;
					updated = true;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return updated ? accountFile : null;
	}

	protected abstract Path getAccountFile();

	protected Path getAccountFile(String pattern, Comparator<? super Path> comparator)
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
