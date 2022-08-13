package com.roddyaj.portfoliomanager.schwab;

import java.nio.file.Path;

import com.roddyaj.portfoliomanager.model.PortfolioState;
import com.roddyaj.schwabparse.SchwabOrdersFile;

public class OrdersMonitor extends AbstractMonitor
{
	public OrdersMonitor(Path dir, String accountName, String accountNumber, PortfolioState state)
	{
		super(dir, accountName, accountNumber, state);
	}

	@Override
	protected Path getFile()
	{
		return getFile(accountNumber + " Order Details.*\\.CSV", (p1, p2) -> SchwabOrdersFile.getTime(p2).compareTo(SchwabOrdersFile.getTime(p1)));
	}

	@Override
	protected void updateState(Path file, PortfolioState state)
	{
		state.setOpenOrders(new SchwabOrdersFile(file).getOpenOrders());
	}
}
