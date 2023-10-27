package com.roddyaj.portfoliomanager.model2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public record Position(String symbol, String description, double quantity, double price, double dayChangePct, double costBasis, double percentOfAccount, Option option)
{
	public double getMarketValue()
	{
		return quantity * price;
	}

	public double getGainLossPct()
	{
		return costBasis != 0 ? (getMarketValue() / costBasis - 1) * 100 : 0;
	}

	public boolean isOption()
	{
		return option != null;
	}
}
