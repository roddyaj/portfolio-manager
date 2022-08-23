package com.roddyaj.portfoliomanager.model;

import com.roddyaj.schwabparse.SchwabOrdersData;
import com.roddyaj.schwabparse.SchwabPositionsData;
import com.roddyaj.schwabparse.SchwabTransactionsData;

public class Portfolio
{
	private SchwabPositionsData positions;

	private SchwabTransactionsData transactions;

	private SchwabOrdersData orders;

	public SchwabPositionsData getPositions()
	{
		return positions;
	}

	public void setPositions(SchwabPositionsData positions)
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
