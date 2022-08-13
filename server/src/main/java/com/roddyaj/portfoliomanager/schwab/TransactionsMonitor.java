package com.roddyaj.portfoliomanager.schwab;

import java.nio.file.Path;

import com.roddyaj.portfoliomanager.model.PortfolioState;
import com.roddyaj.schwabparse.SchwabTransactionsFile;

public class TransactionsMonitor extends AbstractMonitor
{
	public TransactionsMonitor(Path dir, String accountName, String accountNumber, PortfolioState state)
	{
		super(dir, accountName, accountNumber, state);
	}

	@Override
	protected Path getFile()
	{
		final String pattern = ".*_Transactions_.*\\.csv";
		Path file = getFile(accountName + pattern,
			(p1, p2) -> SchwabTransactionsFile.getTime(p2).compareTo(SchwabTransactionsFile.getTime(p1)));
		if (file == null)
		{
			String masked = "XXXXX" + accountNumber.substring(5);
			file = getFile(masked + pattern, (p1, p2) -> SchwabTransactionsFile.getTime(p2).compareTo(SchwabTransactionsFile.getTime(p1)));
		}
		return file;
	}

	@Override
	protected void updateState(Path file, PortfolioState state)
	{
		state.setTransactions(new SchwabTransactionsFile(file).getTransactions());
	}
}
