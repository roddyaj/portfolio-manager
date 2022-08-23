package com.roddyaj.portfoliomanager.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.portfoliomanager.settings.Settings;

public final class State
{
	private static final State INSTANCE = new State();

	public static State getInstance()
	{
		return INSTANCE;
	}

	public Path getInputDir()
	{
		return Paths.get(System.getProperty("user.home"), "Downloads");
	}

	public Settings getSettings()
	{
		Settings settings = null;
		Path settingsFile = Paths.get(Paths.get(System.getProperty("user.home"), ".invest").toString(), "settings.json");
		try
		{
			settings = new ObjectMapper().readValue(settingsFile.toFile(), Settings.class);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return settings;
	}

	private State()
	{
	}
}
