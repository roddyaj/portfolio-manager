package com.roddyaj.portfoliomanager.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.portfoliomanager.settings.Settings;

public final class State
{
	private static final State INSTANCE = new State();

	public static State getInstance()
	{
		return INSTANCE;
	}

	private Instant lastRefresh;

	private Collection<String> symbolsToLookup;

	private final Map<String, Quote> quotes = new ConcurrentHashMap<>();

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

	public synchronized Instant getLastRefresh()
	{
		return lastRefresh;
	}

	public synchronized void setLastRefresh(Instant lastRefresh)
	{
		this.lastRefresh = lastRefresh;
	}

	public synchronized Collection<String> getSymbolsToLookup()
	{
		return symbolsToLookup != null ? new ArrayList<>(symbolsToLookup) : List.of();
	}

	public synchronized void setSymbolsToLookup(Collection<String> symbolsToLookup)
	{
		this.symbolsToLookup = new ArrayList<>(symbolsToLookup);
	}

	public Quote getQuote(String symbol)
	{
		return quotes.get(symbol);
	}

	public void setQuote(String symbol, Quote quote)
	{
		quotes.put(symbol, quote);
	}

	private State()
	{
	}
}
