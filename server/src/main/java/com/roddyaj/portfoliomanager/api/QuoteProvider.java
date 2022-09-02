package com.roddyaj.portfoliomanager.api;

import java.io.IOException;

import com.roddyaj.portfoliomanager.model.Quote;

public interface QuoteProvider
{
	String getName();

	Quote getQuote(String symbol) throws IOException;
}
