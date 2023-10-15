package com.roddyaj.portfoliomanager.model;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record InputPositions(List<InputPosition> positions, double cash, double balance, ZonedDateTime time)
{
	public String toCsvString()
	{
		return positions.stream().map(InputPosition::toCsvString).collect(Collectors.joining("\n"));
	}
}
