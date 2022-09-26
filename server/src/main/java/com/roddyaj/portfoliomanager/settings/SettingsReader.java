package com.roddyaj.portfoliomanager.settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SettingsReader
{
	private static final Path settingsFile = Paths.get(Paths.get(System.getProperty("user.home"), ".invest").toString(), "settings.json");

	private Settings settings;

	private FileTime lastModified;

	public synchronized Settings getSettings()
	{
		try
		{
			FileTime lastModified = Files.getLastModifiedTime(settingsFile);
			if (this.lastModified == null || lastModified.compareTo(this.lastModified) > 0)
			{
				settings = new ObjectMapper().readValue(settingsFile.toFile(), Settings.class);
				this.lastModified = lastModified;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return settings;
	}
}
