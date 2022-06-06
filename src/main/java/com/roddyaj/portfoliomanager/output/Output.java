package com.roddyaj.portfoliomanager.output;

import java.util.ArrayList;
import java.util.List;

public class Output
{
	private String accountName;

	private List<Position> positions;

	public String getAccountName()
	{
		return accountName;
	}

	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
	}

	public List<Position> getPositions()
	{
		if (positions == null)
			positions = new ArrayList<>();
		return positions;
	}

	public void setPositions(List<Position> positions)
	{
		this.positions = positions;
	}
}
