package com.roddyaj.portfoliomanager.model2;

import java.time.LocalDate;

public record Option(String symbol, LocalDate expiryDate, double strike, Type type, double intrinsicValue, boolean inTheMoney)
{
	public double getUnderlyingPrice()
	{
		return type == Type.PUT ? strike - intrinsicValue : strike + intrinsicValue;
	}

	public enum Type
	{
		CALL,
		PUT
	};
}
