package com.roddyaj.portfoliomanager.api.schwab;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.portfoliomanager.api.ParsingUtils;
import com.roddyaj.portfoliomanager.model.Option;
import com.roddyaj.portfoliomanager.model.Order;
import com.roddyaj.portfoliomanager.model.Order.OrderType;

final class SchwabOrdersReader
{
	public static List<Order> readOpenOrders(Path dir, String accountName, String accountNumber)
	{
		String pattern = accountName.replace('_', ' ') + "XXXX" + accountNumber.substring(4) + "_Order_Status_.*\\.csv";
		Comparator<Path> comparator = (p1, p2) -> ParsingUtils.getFileTime(p2).compareTo(ParsingUtils.getFileTime(p1));
		Path file = ParsingUtils.getFile(dir, pattern, comparator);
		return ParsingUtils.readCsv(file, 0).stream().map(SchwabOrdersReader::convertOpenOrder).toList();
	}

	private static Order convertOpenOrder(CSVRecord record)
	{
		String symbol = ParsingUtils.parseString(record.get("Symbol"));
		Option option = SchwabUtils.convertOption(symbol, 0, false);
		if (option != null)
		{
			symbol = option.symbol();
		}

		// @formatter:off
		return new Order(
			symbol,
			SchwabUtils.convertTransactionType(record.get("Action")),
			Double.parseDouble(record.get("Quantity|Face Value").split(" ")[0]),
			getLimitPrice(record.get("Price")),
			convertOrderType(record.get("Price")),
			null,
			option
		);
		// @formatter:on
	}

	private static OrderType convertOrderType(String price)
	{
		String orderType = price.contains(" ") ? price.split(" ")[0] : price;
		return switch (orderType)
		{
			case "Market" -> OrderType.MARKET;
			case "Limit" -> OrderType.LIMIT;
			default -> null;
		};
	}

	private static double getLimitPrice(String price)
	{
		return price.contains(" ") ? ParsingUtils.parseDouble(price.split(" ")[1]) : 0;
	}

	private SchwabOrdersReader()
	{
	}
}
