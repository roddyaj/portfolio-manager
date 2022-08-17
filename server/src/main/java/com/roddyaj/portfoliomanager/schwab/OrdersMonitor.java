package com.roddyaj.portfoliomanager.schwab;

import java.nio.file.Path;

import com.roddyaj.portfoliomanager.model.PortfolioState;
import com.roddyaj.schwabparse.SchwabOrdersReader;

public class OrdersMonitor extends AbstractMonitor
{
	public OrdersMonitor(Path dir, String accountName, String accountNumber, PortfolioState state)
	{
		super(dir, accountName, accountNumber, state);
	}

	@Override
	protected Path getFile()
	{
		return getFile(accountNumber + " Order Details.*\\.CSV", (p1, p2) -> SchwabOrdersReader.getTime(p2).compareTo(SchwabOrdersReader.getTime(p1)));
	}

	@Override
	protected void updateState(Path file, PortfolioState state)
	{
		state.setOrders(new SchwabOrdersReader().read(file));
	}
}