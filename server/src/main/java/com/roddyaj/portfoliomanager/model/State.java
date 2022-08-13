package com.roddyaj.portfoliomanager.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import com.roddyaj.portfoliomanager.settings.Settings;

public final class State
{
	private static final State INSTANCE = new State();

	public static State getInstance()
	{
		return INSTANCE;
	}

	private final AtomicReference<Settings> settings = new AtomicReference<>();

	public Path getInputDir()
	{
		return Paths.get(System.getProperty("user.home"), "Downloads");
	}

	public Settings getSettings()
	{
		return settings.get();
	}

	public void setSettings(Settings settings)
	{
		this.settings.set(settings);
	}

	private State()
	{
	}
}
