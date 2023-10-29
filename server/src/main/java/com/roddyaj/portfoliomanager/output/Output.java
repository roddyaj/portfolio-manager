package com.roddyaj.portfoliomanager.output;

import java.util.ArrayList;
import java.util.List;

public class Output
{
	private String accountName;

	private boolean optionsEnabled;

	private double balance;

	private double cash;

	private double cashOnHold;

	private double openBuyAmount;

	private double cashAvailable;

	private double portfolioReturn;

	private long positionsTime;

	private double sp500YtdReturn;

	private List<OutputPosition> positions;

	private List<MonthlyIncome> income;

	private List<PutToSell> putsToSell;

	public String getAccountName()
	{
		return accountName;
	}

	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
	}

	public boolean isOptionsEnabled()
	{
		return optionsEnabled;
	}

	public void setOptionsEnabled(boolean optionsEnabled)
	{
		this.optionsEnabled = optionsEnabled;
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

	public double getCashOnHold()
	{
		return cashOnHold;
	}

	public void setCashOnHold(double cashOnHold)
	{
		this.cashOnHold = cashOnHold;
	}

	public double getOpenBuyAmount()
	{
		return openBuyAmount;
	}

	public void setOpenBuyAmount(double openBuyAmount)
	{
		this.openBuyAmount = openBuyAmount;
	}

	public double getCashAvailable()
	{
		return cashAvailable;
	}

	public void setCashAvailable(double cashAvailable)
	{
		this.cashAvailable = cashAvailable;
	}

	public double getPortfolioReturn()
	{
		return portfolioReturn;
	}

	public void setPortfolioReturn(double portfolioReturn)
	{
		this.portfolioReturn = portfolioReturn;
	}

	public long getPositionsTime()
	{
		return positionsTime;
	}

	public void setPositionsTime(long positionsTime)
	{
		this.positionsTime = positionsTime;
	}

	public double getSp500YtdReturn()
	{
		return sp500YtdReturn;
	}

	public void setSp500YtdReturn(double sp500YtdReturn)
	{
		this.sp500YtdReturn = sp500YtdReturn;
	}

	public List<OutputPosition> getPositions()
	{
		if (positions == null)
			positions = new ArrayList<>();
		return positions;
	}

	public void setPositions(List<OutputPosition> positions)
	{
		this.positions = positions;
	}

	public List<MonthlyIncome> getIncome()
	{
		return income;
	}

	public void setIncome(List<MonthlyIncome> income)
	{
		this.income = income;
	}

	public List<PutToSell> getPutsToSell()
	{
		return putsToSell;
	}

	public void setPutsToSell(List<PutToSell> putsToSell)
	{
		this.putsToSell = putsToSell;
	}
}
