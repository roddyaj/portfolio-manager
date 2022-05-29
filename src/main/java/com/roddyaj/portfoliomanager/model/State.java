package com.roddyaj.portfoliomanager.model;

import java.util.List;

import com.roddyaj.schwabparse.SchwabOpenOrder;
import com.roddyaj.schwabparse.SchwabPosition;
import com.roddyaj.schwabparse.SchwabTransaction;

public class State
{
	private List<SchwabPosition> positions;

	private List<SchwabTransaction> transactions;

	private List<SchwabOpenOrder> orders;

	public List<SchwabPosition> getPositions()
	{
		return positions;
	}

	public void setPositions(List<SchwabPosition> positions)
	{
		this.positions = positions;
	}

	public List<SchwabTransaction> getTransactions()
	{
		return transactions;
	}

	public void setTransactions(List<SchwabTransaction> transactions)
	{
		this.transactions = transactions;
	}

	public List<SchwabOpenOrder> getOrders()
	{
		return orders;
	}

	public void setOrders(List<SchwabOpenOrder> orders)
	{
		this.orders = orders;
	}
}
