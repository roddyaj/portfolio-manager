package com.roddyaj.portfoliomanager.output;

import java.util.ArrayList;
import java.util.List;

public class Output
{
	private String accountName;

	private double balance;

	private double cash;

	private long positionsTime;

	private List<Position> positions;

	public String getAccountName()
	{
		return accountName;
	}

	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
	}

	public double getBalance()
	{
		return balance;
	}

	public void setBalance(double balance)
	{
		this.balance = balance;
	}

	public double getCash()
	{
		return cash;
	}

	public void setCash(double cash)
	{
		this.cash = cash;
	}

	public long getPositionsTime()
	{
		return positionsTime;
	}

	public void setPositionsTime(long positionsTime)
	{
		this.positionsTime = positionsTime;
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
