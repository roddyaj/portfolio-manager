package com.roddyaj.portfoliomanager.api.schwab;

import java.nio.file.Path;

import com.roddyaj.portfoliomanager.model.Portfolio;
import com.roddyaj.schwabparse.SchwabOrdersReader;

public class OrdersMonitor extends AbstractMonitor
{
	public OrdersMonitor(Path dir, String accountName, String accountNumber, Portfolio state)
	{
		super(dir, accountName, accountNumber, state);
	}

	@Override
	protected Path getFile()
	{
		return getFile(accountName.replace('_', ' ') + "XXXX" + accountNumber.substring(4) + "_Order_Status_.*\\.csv", (p1, p2) -> SchwabOrdersReader.getTime(p2).compareTo(SchwabOrdersReader.getTime(p1)));
	}

	@Override
	protected void updateState(Path file, Portfolio state)
	{
		state.setOrders(new SchwabOrdersReader().read(file));
	}
}
