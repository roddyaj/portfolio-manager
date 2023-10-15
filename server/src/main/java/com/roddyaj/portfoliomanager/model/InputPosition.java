package com.roddyaj.portfoliomanager.model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

public record InputPosition(
	String symbol,
	String description,
	double quantity,
	double price,
	double marketValue,
	double dayChangePct,
	double costBasis,
	double percentOfAccount,
	String optionType,
	LocalDate expiryDate,
	Double strike,
	Double intrinsicValue,
	Double underlyingPrice,
	String actualSymbol,
	boolean inTheMoney)
{
	public double getGainLossPct()
	{
		return costBasis != 0 ? (marketValue / costBasis - 1) * 100 : 0;
	}

	public boolean isOption()
	{
		return optionType != null;
	}

	public String toCsvString()
	{
		return Arrays.asList(symbol, description, quantity, price, marketValue, dayChangePct, costBasis, percentOfAccount, getGainLossPct()).stream()
			.map(o -> o != null ? o.toString() : "").collect(Collectors.joining(","));
	}
}
