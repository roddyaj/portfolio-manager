package com.roddyaj.portfoliomanager;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.roddyaj.portfoliomanager.schwab.AbstractMonitor;
import com.roddyaj.portfoliomanager.schwab.OrdersMonitor;
import com.roddyaj.portfoliomanager.schwab.PositionsMonitor;
import com.roddyaj.portfoliomanager.schwab.TransactionsMonitor;

public final class Main
{
	public static void main(String[] args)
	{
		new Main().run();
	}

	public void run()
	{
		Path dir = Paths.get(System.getProperty("user.home"), "Downloads");
		String accountName = "PCRA";
		String accountNumber = "12345678";

		AbstractMonitor positionsMonitor = new PositionsMonitor(dir, accountName, accountNumber);
		AbstractMonitor transactionsMonitor = new TransactionsMonitor(dir, accountName, accountNumber);
		AbstractMonitor ordersMonitor = new OrdersMonitor(dir, accountName, accountNumber);

		for (int i = 0; i < 5; i++)
		{
			positionsMonitor.check();
			transactionsMonitor.check();
			ordersMonitor.check();

			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
