package com.roddyaj.portfoliomanager.model;

public record Message(Level level, String text)
{
	public enum Level
	{
		INFO,
		WARN,
		ERROR
	}
}
