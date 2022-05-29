package com.roddyaj.portfoliomanager.schwab;

import java.nio.file.Path;

import com.roddyaj.portfoliomanager.model.State;
import com.roddyaj.schwabparse.SchwabPositionsFile;

public class PositionsMonitor extends AbstractMonitor
{
	public PositionsMonitor(Path dir, String accountName, String accountNumber, State state)
	{
		super(dir, accountName, accountNumber, state);
	}

	@Override
	protected Path getFile()
	{
		return getFile(accountName + ".*-Positions-.*\\.CSV",
			(p1, p2) -> SchwabPositionsFile.getTime(p2).compareTo(SchwabPositionsFile.getTime(p1)));
	}

	@Override
	protected void updateState(Path file, State state)
	{
		state.setPositions(new SchwabPositionsFile(file).getPositions());
	}
}
