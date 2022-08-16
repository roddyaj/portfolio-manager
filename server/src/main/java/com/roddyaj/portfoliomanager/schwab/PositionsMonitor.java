package com.roddyaj.portfoliomanager.schwab;

import java.nio.file.Path;

import com.roddyaj.portfoliomanager.model.PortfolioState;
import com.roddyaj.schwabparse.SchwabPositionsReader;

public class PositionsMonitor extends AbstractMonitor
{
	public PositionsMonitor(Path dir, String accountName, String accountNumber, PortfolioState state)
	{
		super(dir, accountName, accountNumber, state);
	}

	@Override
	protected Path getFile()
	{
		return getFile(accountName + ".*-Positions-.*\\.CSV",
			(p1, p2) -> SchwabPositionsReader.getTime(p2).compareTo(SchwabPositionsReader.getTime(p1)));
	}

	@Override
	protected void updateState(Path file, PortfolioState state)
	{
		state.setPositions(new SchwabPositionsReader().read(file));
	}
}
