package com.roddyaj.portfoliomanager;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.roddyaj.portfoliomanager.model.State;
import com.roddyaj.portfoliomanager.output.Order;
import com.roddyaj.portfoliomanager.output.Output;
import com.roddyaj.portfoliomanager.output.Position;
import com.roddyaj.portfoliomanager.output.Transaction;
import com.roddyaj.portfoliomanager.schwab.AbstractMonitor;
import com.roddyaj.portfoliomanager.schwab.OrdersMonitor;
import com.roddyaj.portfoliomanager.schwab.PositionsMonitor;
import com.roddyaj.portfoliomanager.schwab.TransactionsMonitor;
import com.roddyaj.schwabparse.SchwabOrder;
import com.roddyaj.schwabparse.SchwabPosition;
import com.roddyaj.schwabparse.SchwabTransaction;

public final class Main
{
	public static void main(String[] args)
	{
		Path inputDir = Paths.get(System.getProperty("user.home"), "Downloads");
		String accountName = "PCRA";
		String accountNumber = "12345678";

		new Main().run(inputDir, accountName, accountNumber);
	}

	public void run(Path inputDir, String accountName, String accountNumber)
	{
		State state = new State();

		List<AbstractMonitor> monitors = new ArrayList<>();
		monitors.add(new PositionsMonitor(inputDir, accountName, accountNumber, state));
		monitors.add(new TransactionsMonitor(inputDir, accountName, accountNumber, state));
		monitors.add(new OrdersMonitor(inputDir, accountName, accountNumber, state));

		for (int i = 0; i < 1; i++)
		{
			boolean anyUpdated = false;
			for (AbstractMonitor monitor : monitors)
				anyUpdated |= monitor.check();

			if (anyUpdated)
			{
				Output output = createOutput(accountName, state);
				writeOutput(output);
			}

//			try
//			{
//				Thread.sleep(5000);
//			}
//			catch (InterruptedException e)
//			{
//				e.printStackTrace();
//			}
		}
	}

	private Output createOutput(String accountName, State state)
	{
		Output output = new Output();
		output.setAccountName(accountName);

		Map<String, List<SchwabTransaction>> symbolToTransactions = state.getTransactions().stream()
			.filter(t -> t.symbol() != null && t.quantity() != null && t.price() != null).collect(Collectors.groupingBy(SchwabTransaction::symbol));
		Map<String, List<SchwabOrder>> symbolToOrders = state.getOpenOrders().stream().filter(o -> o.symbol() != null)
			.collect(Collectors.groupingBy(SchwabOrder::symbol));

		for (SchwabPosition schwabPosition : state.getPositions())
		{
			if (schwabPosition.quantity() != null)
			{
				Position position = new Position();
				position.setSymbol(schwabPosition.symbol());
				position.setDescription(schwabPosition.description());
				position.setQuantity(schwabPosition.quantity().intValue());
				position.setPrice(schwabPosition.price().doubleValue());
				position.setMarketValue(schwabPosition.marketValue());
				position.setCostBasis(schwabPosition.costBasis().doubleValue());
				position.setDayChangePct(schwabPosition.dayChangePct().doubleValue());
				position.setGainLossPct(schwabPosition.gainLossPct().doubleValue());
				position.setPercentOfAccount(schwabPosition.percentOfAccount());
				position.setDividendYield(schwabPosition.dividendYield());
				position.set52WeekLow(schwabPosition._52WeekLow());
				position.set52WeekHigh(schwabPosition._52WeekHigh());

				for (SchwabTransaction schwabTransaction : symbolToTransactions.getOrDefault(schwabPosition.symbol(), List.of()))
				{
					Transaction transaction = new Transaction();
					transaction.setDate(schwabTransaction.date().toString());
					transaction.setAction(schwabTransaction.action());
					transaction.setQuantity(schwabTransaction.quantity().doubleValue());
					transaction.setPrice(schwabTransaction.price().doubleValue());
					transaction.setAmount(schwabTransaction.amount().doubleValue());

					position.addTransaction(transaction);
				}

				for (SchwabOrder schwabOrder : symbolToOrders.getOrDefault(schwabPosition.symbol(), List.of()))
				{
					Order order = new Order();
					order.setAction(schwabOrder.action());
					order.setQuantity(schwabOrder.quantity());
					order.setOrderType(schwabOrder.orderType());
					order.setLimitPrice(schwabOrder.limitPrice());
					order.setTiming(schwabOrder.timing().toString());

					position.addOpenOrder(order);
				}

				output.getPositions().add(position);
			}
		}
		return output;
	}

	private void writeOutput(Output output)
	{
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.writeValue(System.out, output);

//			Path outputPath = Paths.get(System.getProperty("user.home"), "Desktop", "output.json");
//			new ObjectMapper().writeValue(outputPath.toFile(), output);
//			System.out.println("Wrote to " + outputPath);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
