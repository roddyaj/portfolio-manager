package com.roddyaj.portfoliomanager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.roddyaj.portfoliomanager.model.State;
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

		State state = new State();

		List<AbstractMonitor> monitors = new ArrayList<>();
		monitors.add(new PositionsMonitor(dir, accountName, accountNumber, state));
		monitors.add(new TransactionsMonitor(dir, accountName, accountNumber, state));
		monitors.add(new OrdersMonitor(dir, accountName, accountNumber, state));

		for (int i = 0; i < 5; i++)
		{
			boolean anyUpdated = false;
			for (AbstractMonitor monitor : monitors)
			{
				anyUpdated |= monitor.check();
			}

			if (anyUpdated)
			{
				System.out.println(state.getPositions().size());
				System.out.println(state.getTransactions().size());
				System.out.println(state.getOrders().size());
			}

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
