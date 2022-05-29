package com.roddyaj.portfoliomanager.schwab;

import java.nio.file.Path;

import com.roddyaj.schwabparse.SchwabPositionsFile;

public class PositionsMonitor extends AbstractMonitor
{
	public PositionsMonitor(Path dir, String accountName, String accountNumber)
	{
		super(dir, accountName, accountNumber);
	}

	@Override
	protected Path getAccountFile()
	{
		return getAccountFile(accountName + ".*-Positions-.*\\.CSV",
			(p1, p2) -> SchwabPositionsFile.getTime(p2).compareTo(SchwabPositionsFile.getTime(p1)));
	}
}
