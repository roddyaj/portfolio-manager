package com.roddyaj.portfoliomanager.output;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Position
{
	private String symbol;
	private String description;
	private int quantity;
	private double price;
	private double marketValue;
	private double costBasis;
//	Double priceChange;
//	Double priceChangePct;
//	Double dayChange;
	private double dayChangePct;
//	Double gainLoss;
	private double gainLossPct;
//	boolean reinvestDividends;
//	boolean capitalGains;
	Double percentOfAccount;
	Double dividendYield;
//	Double lastDividend;
//	LocalDate exDividendDate;
//	Double peRatio;
	Double _52WeekLow;
	Double _52WeekHigh;
//	Integer volume;
//	Double intrinsicValue;
//	String inTheMoney;
//	String securityType;

	private List<Transaction> transactions;
	private List<Order> openOrders;

	public String getSymbol()
	{
		return symbol;
	}

	public void setSymbol(String symbol)
	{
		this.symbol = symbol;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}

	public double getPrice()
	{
		return price;
	}

	public void setPrice(double price)
	{
		this.price = price;
	}

	public double getMarketValue()
	{
		return marketValue;
	}

	public void setMarketValue(double marketValue)
	{
		this.marketValue = marketValue;
	}

	public double getCostBasis()
	{
		return costBasis;
	}

	public void setCostBasis(double costBasis)
	{
		this.costBasis = costBasis;
	}

	public double getDayChangePct()
	{
		return dayChangePct;
	}

	public void setDayChangePct(double dayChangePct)
	{
		this.dayChangePct = dayChangePct;
	}

	public double getGainLossPct()
	{
		return gainLossPct;
	}

	public void setGainLossPct(double gainLossPct)
	{
		this.gainLossPct = gainLossPct;
	}

	public Double getPercentOfAccount()
	{
		return percentOfAccount;
	}

	public void setPercentOfAccount(Double percentOfAccount)
	{
		this.percentOfAccount = percentOfAccount;
	}

	public Double getDividendYield()
	{
		return dividendYield;
	}

	public void setDividendYield(Double dividendYield)
	{
		this.dividendYield = dividendYield;
	}

	public Double get52WeekLow()
	{
		return _52WeekLow;
	}

	public void set52WeekLow(Double _52WeekLow)
	{
		this._52WeekLow = _52WeekLow;
	}

	public Double get52WeekHigh()
	{
		return _52WeekHigh;
	}

	public void set52WeekHigh(Double _52WeekHigh)
	{
		this._52WeekHigh = _52WeekHigh;
	}

	public List<Transaction> getTransactions()
	{
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions)
	{
		this.transactions = transactions;
	}

	public void addTransaction(Transaction transaction)
	{
		if (transactions == null)
			transactions = new ArrayList<>();
		this.transactions.add(transaction);
	}

	public List<Order> getOpenOrders()
	{
		return openOrders;
	}

	public void setOpenOrders(List<Order> openOrders)
	{
		this.openOrders = openOrders;
	}

	public void addOpenOrder(Order openOrder)
	{
		if (openOrders == null)
			openOrders = new ArrayList<>();
		this.openOrders.add(openOrder);
	}
}
