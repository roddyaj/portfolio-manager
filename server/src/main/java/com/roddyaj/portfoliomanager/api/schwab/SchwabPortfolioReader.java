package com.roddyaj.portfoliomanager.api.schwab;

import java.nio.file.Path;

import com.roddyaj.portfoliomanager.api.PortfolioReader;
import com.roddyaj.portfoliomanager.api.schwab.SchwabPositionsReader.PositionInfo;
import com.roddyaj.portfoliomanager.model.Portfolio;

public class SchwabPortfolioReader implements PortfolioReader
{
	@Override
	public Portfolio read(Path dir, String accountName, String accountNumber)
	{
		PositionInfo positionInfo = SchwabPositionsReader.readPositions(dir, accountName);
		// @formatter:off
		return new Portfolio(
			positionInfo.positions(),
			SchwabOrdersReader.readOpenOrders(dir, accountName, accountNumber),
			SchwabTransactionsReader.readTransactions(dir, accountName, accountNumber),
			positionInfo.cash(),
			positionInfo.balance(),
			positionInfo.time());
		// @formatter:on
	}
}
