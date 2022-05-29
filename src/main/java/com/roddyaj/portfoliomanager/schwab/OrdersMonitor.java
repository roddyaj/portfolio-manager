package com.roddyaj.portfoliomanager.schwab;

import java.nio.file.Path;

import com.roddyaj.portfoliomanager.model.State;
import com.roddyaj.schwabparse.SchwabOpenOrdersFile;

public class OrdersMonitor extends AbstractMonitor
{
	public OrdersMonitor(Path dir, String accountName, String accountNumber, State state)
	{
		super(dir, accountName, accountNumber, state);
	}

	@Override
	protected Path getFile()
	{
		return getFile(accountNumber + " Order Details.*\\.CSV",
			(p1, p2) -> SchwabOpenOrdersFile.getTime(p2).compareTo(SchwabOpenOrdersFile.getTime(p1)));
	}

	@Override
	protected void updateState(Path file, State state)
	{
		state.setOrders(new SchwabOpenOrdersFile(file).getOpenOrders());
	}
}
