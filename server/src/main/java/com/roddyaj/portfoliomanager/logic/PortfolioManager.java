package com.roddyaj.portfoliomanager.logic;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.roddyaj.portfoliomanager.model.Message;
import com.roddyaj.portfoliomanager.model.PortfolioState;
import com.roddyaj.portfoliomanager.output.Order;
import com.roddyaj.portfoliomanager.output.Output;
import com.roddyaj.portfoliomanager.output.Position;
import com.roddyaj.portfoliomanager.output.Transaction;
import com.roddyaj.portfoliomanager.schwab.AbstractMonitor;
import com.roddyaj.portfoliomanager.schwab.OrdersMonitor;
import com.roddyaj.portfoliomanager.schwab.PositionsMonitor;
import com.roddyaj.portfoliomanager.schwab.TransactionsMonitor;
import com.roddyaj.portfoliomanager.settings.AccountSettings;
import com.roddyaj.schwabparse.SchwabOrder;
import com.roddyaj.schwabparse.SchwabOrdersData;
import com.roddyaj.schwabparse.SchwabPosition;
import com.roddyaj.schwabparse.SchwabPositionsData;
import com.roddyaj.schwabparse.SchwabTransaction;
import com.roddyaj.schwabparse.SchwabTransactionsData;

public final class PortfolioManager
{
	public Output process(Path inputDir, String accountName, String accountNumber, AccountSettings accountSettings)
	{
		PortfolioState state = new PortfolioState();

		List<AbstractMonitor> monitors = new ArrayList<>();
		monitors.add(new PositionsMonitor(inputDir, accountName, accountNumber, state));
		monitors.add(new TransactionsMonitor(inputDir, accountName, accountNumber, state));
		monitors.add(new OrdersMonitor(inputDir, accountName, accountNumber, state));

		boolean anyUpdated = false;
		for (AbstractMonitor monitor : monitors)
			anyUpdated |= monitor.check();

		Output output = null;
		if (anyUpdated)
			output = createOutput(accountName, accountSettings, state);

		return output;
	}

	private Output createOutput(String accountName, AccountSettings accountSettings, PortfolioState state)
	{
		Output output = new Output();
		output.setAccountName(accountName);

		SchwabTransactionsData transactions = state.getTransactions();
		Map<String, List<SchwabTransaction>> symbolToTransactions = transactions != null ? transactions.transactions().stream()
			.filter(t -> t.symbol() != null && t.quantity() != null && t.price() != null).collect(Collectors.groupingBy(SchwabTransaction::symbol))
			: Map.of();

		SchwabOrdersData orders = state.getOrders();
		Map<String, List<SchwabOrder>> symbolToOrders = orders != null
			? orders.getOpenOrders().stream().filter(o -> o.symbol() != null).collect(Collectors.groupingBy(SchwabOrder::symbol))
			: Map.of();

		List<Message> messages = new ArrayList<>();
		AllocationMap allocationMap = new AllocationMap(accountSettings.getAllocations(), messages);

		SchwabPositionsData positions = state.getPositions();
		if (positions != null)
		{
			Map<String, List<SchwabPosition>> symbolToOptions = positions.positions().stream().filter(p -> p.symbol().contains(" "))
				.collect(Collectors.groupingBy(p -> p.symbol().split(" ")[0]));

			output.setBalance(positions.balance());
			output.setCash(positions.cash());
			output.setPositionsTime(positions.time().toEpochSecond() * 1000);
			for (SchwabPosition schwabPosition : positions.positions())
			{
				Position position = new Position();
				position.setSymbol(schwabPosition.symbol());
				position.setDescription(schwabPosition.description());
				position.setQuantity(schwabPosition.quantity().intValue());
				position.setPrice(schwabPosition.price().doubleValue());
				position.setMarketValue(schwabPosition.marketValue());
				position.setCostBasis(schwabPosition.costBasis() != null ? schwabPosition.costBasis().doubleValue() : 0);
				position.setDayChangePct(schwabPosition.dayChangePct() != null ? schwabPosition.dayChangePct().doubleValue() : 0);
				position.setGainLossPct(schwabPosition.gainLossPct() != null ? schwabPosition.gainLossPct().doubleValue() : 0);
				position.setPercentOfAccount(schwabPosition.percentOfAccount());
				position.setDividendYield(schwabPosition.dividendYield());
				position.setPeRatio(schwabPosition.peRatio());
				position.set52WeekLow(schwabPosition._52WeekLow());
				position.set52WeekHigh(schwabPosition._52WeekHigh());
				Double target = allocationMap.getAllocation(schwabPosition.symbol());
				position.setTargetPct(target != null ? (target.doubleValue() * 100) : null);
				position.setSharesToBuy(calculateSharesToBuy(position, accountSettings, positions.balance(), target));

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

	private Integer calculateSharesToBuy(Position position, AccountSettings accountSettings, double accountBalance, Double target)
	{
		Integer sharesToBuy = null;
		if (target != null)
		{
			double targetValue = accountBalance * target.doubleValue();
			double delta = targetValue - position.getMarketValue();
			int quantity = round(delta / position.getPrice(), .75);
			boolean isBuy = quantity > 0;
			boolean doOrder = quantity != 0 && Math.abs(delta / targetValue) > (isBuy ? 0.005 : 0.02)
				&& Math.abs(quantity * position.getPrice()) >= accountSettings.getMinOrder();
			if (doOrder)
				sharesToBuy = quantity;
		}
		return sharesToBuy;
	}

	static int round(double value, double cutoff)
	{
		return (int)(value >= 0 ? value + (1 - cutoff) : value - (1 - cutoff));
	}
}
