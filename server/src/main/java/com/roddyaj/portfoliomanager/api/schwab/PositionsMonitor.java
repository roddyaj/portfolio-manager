package com.roddyaj.portfoliomanager.api.schwab;

import java.nio.file.Path;

import com.roddyaj.portfoliomanager.api.PositionsReader;
import com.roddyaj.portfoliomanager.api.fidelity.FidelityPositionsReader;
import com.roddyaj.portfoliomanager.api.schwab2.SchwabPositionsReader;
import com.roddyaj.portfoliomanager.model.Portfolio;

public class PositionsMonitor extends AbstractMonitor
{
	private final PositionsReader reader;

	public PositionsMonitor(Path dir, String accountName, String accountNumber, Portfolio state)
	{
		super(dir, accountName, accountNumber, state);
		reader = accountNumber.length() == 9 ?
			new FidelityPositionsReader(dir, accountName) :
			new SchwabPositionsReader(dir, accountName);
	}

	@Override
	protected Path getFile()
	{
		return reader.getFile();
	}

	@Override
	protected void updateState(Path file, Portfolio state)
	{
		state.setPositions(reader.read(file));
	}
}
