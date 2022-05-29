package com.roddyaj.portfoliomanager.schwab;

import java.nio.file.Path;

import com.roddyaj.schwabparse.SchwabOpenOrdersFile;

public class OrdersMonitor extends AbstractMonitor
{
	public OrdersMonitor(Path dir, String accountName, String accountNumber)
	{
		super(dir, accountName, accountNumber);
	}

	@Override
	protected Path getAccountFile()
	{
		return getAccountFile(accountNumber + " Order Details.*\\.CSV",
			(p1, p2) -> SchwabOpenOrdersFile.getTime(p2).compareTo(SchwabOpenOrdersFile.getTime(p1)));
	}
}
