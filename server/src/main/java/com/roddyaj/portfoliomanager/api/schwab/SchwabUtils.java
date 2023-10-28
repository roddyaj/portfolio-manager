package com.roddyaj.portfoliomanager.api.schwab;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.roddyaj.portfoliomanager.model.Option;
import com.roddyaj.portfoliomanager.model.Order.TransactionType;

final class SchwabUtils
{
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");

	public static TransactionType convertTransactionType(String action)
	{
		return switch (action)
		{
			case "Buy" -> TransactionType.BUY;
			case "Sell" -> TransactionType.SELL;
			case "Sell to Open" -> TransactionType.SELL_TO_OPEN;
			case "Buy to Close" -> TransactionType.BUY_TO_CLOSE;
			case "Cash Dividend", "Qualified Dividend" -> TransactionType.DIVIDEND;
			case "Journal", "MoneyLink Deposit", "MoneyLink Transfer", "Funds Received", "Bank Transfer" -> TransactionType.TRANSFER;
			default -> null;
		};
	}

	public static Option convertOption(String text, double intrinsicValue, boolean inTheMoney)
	{
		if (!isOption(text))
			return null;

		String[] tokens = text.split(" ");
		String symbol = tokens[0];
		LocalDate expiryDate = LocalDate.parse(tokens[1], DATE_FORMAT);
		double strike = Double.parseDouble(tokens[2]);
		Option.Type type = switch (tokens[3])
		{
			case "C" -> Option.Type.CALL;
			case "P" -> Option.Type.PUT;
			default -> null;
		};
		return new Option(symbol, expiryDate, strike, type, intrinsicValue, inTheMoney);
	}

	private static boolean isOption(String symbol)
	{
		return symbol != null && symbol.indexOf(' ') != -1 && (symbol.endsWith("C") || symbol.endsWith("P"));
	}

	private SchwabUtils()
	{
	}
}
