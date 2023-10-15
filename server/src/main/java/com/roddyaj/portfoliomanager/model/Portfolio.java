package com.roddyaj.portfoliomanager.model;

import com.roddyaj.schwabparse.SchwabOrdersData;
import com.roddyaj.schwabparse.SchwabTransactionsData;

public class Portfolio
{
	private InputPositions positions;

	private SchwabTransactionsData transactions;

	private SchwabOrdersData orders;

	public InputPositions getPositions()
	{
		return positions;
	}

	public void setPositions(InputPositions positions)
	{
		this.positions = positions;
	}

	public SchwabTransactionsData getTransactions()
	{
		return transactions;
	}

	public void setTransactions(SchwabTransactionsData transactions)
	{
		this.transactions = transactions;
	}

	public SchwabOrdersData getOrders()
	{
		return orders;
	}

	public void setOrders(SchwabOrdersData orders)
	{
		this.orders = orders;
	}
}
