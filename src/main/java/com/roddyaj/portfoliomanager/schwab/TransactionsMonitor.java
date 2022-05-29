package com.roddyaj.portfoliomanager.schwab;

import java.nio.file.Path;

import com.roddyaj.schwabparse.SchwabTransactionsFile;

public class TransactionsMonitor extends AbstractMonitor
{
	public TransactionsMonitor(Path dir, String accountName, String accountNumber)
	{
		super(dir, accountName, accountNumber);
	}

	@Override
	protected Path getAccountFile()
	{
		final String pattern = "_Transactions_.*\\.CSV";
		Path file = getAccountFile(accountName + pattern,
			(p1, p2) -> SchwabTransactionsFile.getTime(p2).compareTo(SchwabTransactionsFile.getTime(p1)));
		if (file == null)
		{
			String masked = "XXXX" + accountNumber.substring(4);
			file = getAccountFile(masked + pattern, (p1, p2) -> SchwabTransactionsFile.getTime(p2).compareTo(SchwabTransactionsFile.getTime(p1)));
		}
		return file;
	}
}
