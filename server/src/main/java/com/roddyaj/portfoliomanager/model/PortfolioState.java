package com.roddyaj.portfoliomanager.model;

import java.util.List;

import com.roddyaj.schwabparse.SchwabOrder;
import com.roddyaj.schwabparse.SchwabPosition;
import com.roddyaj.schwabparse.SchwabTransaction;

public class PortfolioState
{
	private List<SchwabPosition> positions;

	private List<SchwabTransaction> transactions;

	private List<SchwabOrder> openOrders;

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

	public List<SchwabOrder> getOpenOrders()
	{
		return openOrders;
	}

	public void setOpenOrders(List<SchwabOrder> orders)
	{
		this.openOrders = orders;
	}
}
