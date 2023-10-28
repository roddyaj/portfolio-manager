package com.roddyaj.portfoliomanager.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/** Represents an order or transaction. */
@JsonInclude(Include.NON_NULL)
public record Order(String symbol, TransactionType transactionType, double quantity, double price, OrderType orderType, LocalDate date, Option option)
{
	public double getAmount()
	{
		return quantity * price;
	}

	public boolean isOption()
	{
		return option != null;
	}

	public enum TransactionType
	{
		BUY,
		SELL,
		SELL_TO_OPEN,
		BUY_TO_CLOSE,
		DIVIDEND,
		TRANSFER,
	}

	public enum OrderType
	{
		MARKET,
		LIMIT,
	}
}
