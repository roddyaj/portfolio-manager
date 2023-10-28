package com.roddyaj.portfoliomanager.api;

import java.nio.file.Path;

import com.roddyaj.portfoliomanager.model.Portfolio;

public interface PortfolioReader
{
	Portfolio read(Path dir, String accountName, String accountNumber);
}
