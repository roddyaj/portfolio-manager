package com.roddyaj.portfoliomanager.logic;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;

import com.roddyaj.portfoliomanager.api.FinnhubAPI;
import com.roddyaj.portfoliomanager.api.SP500ReturnAPI;
import com.roddyaj.portfoliomanager.model.Message;
import com.roddyaj.portfoliomanager.model.Portfolio;
import com.roddyaj.portfoliomanager.model.Quote;
import com.roddyaj.portfoliomanager.model.State;
import com.roddyaj.portfoliomanager.output.MonthlyIncome;
import com.roddyaj.portfoliomanager.output.Order;
import com.roddyaj.portfoliomanager.output.Output;
import com.roddyaj.portfoliomanager.output.Position;
import com.roddyaj.portfoliomanager.output.PutToSell;
import com.roddyaj.portfoliomanager.output.Transaction;
import com.roddyaj.portfoliomanager.schwab.AbstractMonitor;
import com.roddyaj.portfoliomanager.schwab.OrdersMonitor;
import com.roddyaj.portfoliomanager.schwab.PositionsMonitor;
import com.roddyaj.portfoliomanager.schwab.TransactionsMonitor;
import com.roddyaj.portfoliomanager.settings.AccountSettings;
import com.roddyaj.portfoliomanager.settings.Allocation;
import com.roddyaj.portfoliomanager.settings.Api;
import com.roddyaj.portfoliomanager.settings.Settings;
import com.roddyaj.schwabparse.SchwabOrder;
import com.roddyaj.schwabparse.SchwabOrdersData;
import com.roddyaj.schwabparse.SchwabPosition;
import com.roddyaj.schwabparse.SchwabPositionsData;
import com.roddyaj.schwabparse.SchwabTransaction;
import com.roddyaj.schwabparse.SchwabTransactionsData;

public final class PortfolioManager
{
	private static final Set<String> shortOptionActions = Set.of("Sell to Open", "Buy to Close");
	private static final Set<String> transferActions = Set.of("Journal", "MoneyLink Deposit", "MoneyLink Transfer", "Funds Received",
		"Bank Transfer");

	private final FinnhubAPI finnhubAPI;

	public PortfolioManager(Settings settings)
	{
		finnhubAPI = new FinnhubAPI();
		Api apiSettings = settings.getApi(finnhubAPI.getName());
		if (apiSettings != null)
		{
			finnhubAPI.setApiKey(apiSettings.getApiKey());
			finnhubAPI.setRequestLimitPerMinute(apiSettings.getRequestsPerMinute());
		}
	}

	public Output process(Path inputDir, String accountName, Settings settings)
	{
		String accountNumber = Stream.of(settings.getAccounts()).filter(a -> a.getName().equals(accountName)).map(AccountSettings::getAccountNumber)
			.findAny().orElse(null);
		AccountSettings accountSettings = settings.getAccount(accountName);

		Portfolio portfolio = new Portfolio();

		List<AbstractMonitor> monitors = new ArrayList<>();
		monitors.add(new PositionsMonitor(inputDir, accountName, accountNumber, portfolio));
		monitors.add(new TransactionsMonitor(inputDir, accountName, accountNumber, portfolio));
		monitors.add(new OrdersMonitor(inputDir, accountName, accountNumber, portfolio));

		boolean anyUpdated = false;
		for (AbstractMonitor monitor : monitors)
			anyUpdated |= monitor.check();

		Output output = null;
		if (anyUpdated)
			output = createOutput(accountName, settings, accountSettings, portfolio);

		return output;
	}

	private Output createOutput(String accountName, Settings settings, AccountSettings accountSettings, Portfolio portfolio)
	{
		Output output = new Output();
		output.setAccountName(accountName);
		output.setOptionsEnabled(accountSettings.isOptionsEnabled());

		SchwabTransactionsData transactions = portfolio.getTransactions();
		Map<String, List<SchwabTransaction>> symbolToTransactions = transactions != null
			? transactions.transactions().stream().filter(t -> t.symbol() != null && t.quantity() != null && t.price() != null).collect(
				Collectors.groupingBy(SchwabTransaction::getActualSymbol))
			: Map.of();

		SchwabOrdersData orders = portfolio.getOrders();
		Map<String, List<SchwabOrder>> symbolToOrders = orders != null
			? orders.getOpenOrders().stream().filter(o -> o.symbol() != null).collect(Collectors.groupingBy(SchwabOrder::getActualSymbol))
			: Map.of();

		List<Message> messages = new ArrayList<>();
		AllocationMap allocationMap = new AllocationMap(accountSettings.getAllocations(), messages);

		State state = State.getInstance();

		SchwabPositionsData positions = portfolio.getPositions();
		if (positions != null)
		{
			Instant portfolioTime = positions.time().toInstant();

			Map<String, List<SchwabPosition>> symbolToOptions = positions.positions().stream().filter(SchwabPosition::isOption)
				.collect(Collectors.groupingBy(SchwabPosition::getActualSymbol));
			Map<String, SchwabPosition> symbolToPosition = positions.positions().stream().filter(p -> !p.isOption())
				.collect(Collectors.toMap(SchwabPosition::symbol, Function.identity()));

			output.setBalance(positions.balance());
			output.setCash(positions.cash());
			output.setPortfolioReturn(calculateReturn(accountSettings, portfolio));
			output.setPositionsTime(positions.time().toEpochSecond() * 1000);

			List<Position> allPositions = new ArrayList<>();
			allPositions.addAll(positions.positions().stream().map(PortfolioManager::toPosition).toList());
			allPositions.addAll(getNewPositions(positions.positions(), orders, allocationMap));
			for (Position position : allPositions)
			{
				String symbol = position.getSymbol();
				position.setTransactions(symbolToTransactions.getOrDefault(symbol, List.of()).stream().map(PortfolioManager::toTransaction).toList());
				position.setOpenOrders(symbolToOrders.getOrDefault(symbol, List.of()).stream().map(PortfolioManager::toOrder).toList());
				position.setOptions(symbolToOptions.getOrDefault(symbol, List.of()).stream().map(PortfolioManager::toPosition).toList());

				Quote quote = state.getQuote(symbol);
				if (quote != null && (quote.time().isAfter(portfolioTime) || position.getQuantity() == 0))
				{
					double marketValue = quote.price() * position.getQuantity();
					double gainLossPct = position.getCostBasis() > 0 ? (marketValue / position.getCostBasis() - 1) * 100 : 0;
					position.setPrice(quote.price());
					position.setMarketValue(marketValue);
					position.setDayChangePct(quote.changePct());
					position.setGainLossPct(gainLossPct);
//					position.setPercentOfAccount();
				}

				JsonNode companyInfo = state.getCompanyInfo(symbol);
				if (companyInfo != null)
				{
					position.setLogo(companyInfo.get("logo").textValue());
				}

				Double target = allocationMap.getAllocation(symbol);
				position.setTargetPct(target != null ? (target.doubleValue() * 100) : null);
				position.setSharesToBuy(calculateSharesToBuy(position, accountSettings, positions.balance(), target));
				position.setCallsToSell(calculateCallsToSell(position, settings, accountSettings));

				output.getPositions().add(position);
			}

			output.setCashOnHold(calculateCashOnHold(allPositions));
			output.setOpenBuyAmount(calculateOpenBuyAmount(orders));
			output.setCashAvailable(output.getCash() - output.getCashOnHold() - output.getOpenBuyAmount());

			output.setPutsToSell(Stream.of(settings.getOptionsInclude()).map(s -> {
				return new PutToSell(s, symbolToPosition.containsKey(s) ? symbolToPosition.get(s).price() : null,
					symbolToPosition.containsKey(s) ? symbolToPosition.get(s).dayChangePct() : null);
			}).toList());

			List<String> allSymbols = allPositions.stream().filter(p -> !p.isOption()).map(Position::getSymbol).toList();
			state.setSymbolsToLookup(allSymbols);
		}

		if (transactions != null)
			output.setIncome(calculateIncome(transactions.transactions()));

		state.setLastRefresh(Instant.now());

		output.setSp500YtdReturn(SP500ReturnAPI.getYtdSp500Return());

		return output;
	}

	private static Position toPosition(SchwabPosition schwabPosition)
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
		if (schwabPosition.isOption())
		{
			LocalDate expiryDate = schwabPosition.option().expiryDate();
			double strike = schwabPosition.option().strike();
			String type = schwabPosition.option().type();
			if (schwabPosition.intrinsicValue() != null)
				position.setUnderlyingPrice(type.equals("P") ? strike - schwabPosition.intrinsicValue() : strike + schwabPosition.intrinsicValue());
			position.setInTheMoney("ITM".equals(schwabPosition.inTheMoney()));
			position.setDte((int)ChronoUnit.DAYS.between(LocalDate.now(), expiryDate));
		}
		return position;
	}

	private static Transaction toTransaction(SchwabTransaction schwabTransaction)
	{
		Transaction transaction = new Transaction();
		transaction.setDate(schwabTransaction.date().toString());
		transaction.setAction(schwabTransaction.action());
		transaction.setQuantity(schwabTransaction.quantity().doubleValue());
		transaction.setPrice(schwabTransaction.price().doubleValue());
		transaction.setAmount(schwabTransaction.amount().doubleValue());
		if (schwabTransaction.isOption())
		{
			transaction.setStrike(schwabTransaction.option().strike());
			transaction.setType(schwabTransaction.option().type());
		}
		return transaction;
	}

	private static Order toOrder(SchwabOrder schwabOrder)
	{
		Order order = new Order();
		order.setAction(schwabOrder.action());
		order.setQuantity(schwabOrder.quantity());
		order.setOrderType(schwabOrder.orderType());
		order.setLimitPrice(schwabOrder.limitPrice());
//		order.setTiming(schwabOrder.timing().toString());
		if (schwabOrder.isOption())
		{
			order.setStrike(schwabOrder.option().strike());
			order.setType(schwabOrder.option().type());
		}
		return order;
	}

	private List<Position> getNewPositions(List<? extends SchwabPosition> positions, SchwabOrdersData orders, AllocationMap allocationMap)
	{
		List<Position> newPositions = new ArrayList<>();

		Set<String> positionSymbols = positions.stream().map(SchwabPosition::symbol).collect(Collectors.toSet());
		Set<String> buySymbols = orders != null
			? orders.getOpenOrders().stream().filter(o -> "Buy".equals(o.action())).map(SchwabOrder::symbol).collect(Collectors.toSet())
			: Set.of();
		Set<String> newSymbols = new HashSet<>();
		newSymbols.addAll(allocationMap.getSymbols());
		newSymbols.addAll(buySymbols);
		newSymbols.removeAll(positionSymbols);

		for (String symbol : newSymbols)
		{
			Position position = new Position();
			position.setSymbol(symbol);
			double price = 1;
			try
			{
				Quote quote = finnhubAPI.getQuote(symbol);
				if (quote != null)
					price = quote.price();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			position.setPrice(price);
			newPositions.add(position);
		}

		return newPositions;
	}

	private static Integer calculateSharesToBuy(Position position, AccountSettings accountSettings, double accountBalance, Double target)
	{
		Integer sharesToBuy = null;
		if (target != null)
		{
			double targetValue = accountBalance * target.doubleValue();
			double delta = targetValue - position.getMarketValue();
			int quantity = round(delta / position.getPrice(), .75);
			boolean isBuy = quantity > 0;
			double orderAmount = Math.abs(quantity * position.getPrice());
			Allocation allocation = accountSettings.getAllocation(position.getSymbol());
			double positionMinOrder = allocation != null && allocation.getMinOrder() != null ? allocation.getMinOrder().doubleValue() : 0;
			boolean doOrder = quantity != 0 && Math.abs(delta / targetValue) > (isBuy ? 0.005 : 0.01) && orderAmount >= accountSettings.getMinOrder()
				&& orderAmount >= positionMinOrder;
			if (doOrder)
				sharesToBuy = quantity;
		}
		return sharesToBuy;
	}

	private static Integer calculateCallsToSell(Position position, Settings settings, AccountSettings accountSettings)
	{
		Integer availableCalls = null;
		if (accountSettings.isOptionsEnabled() && !position.isOption() && position.getQuantity() >= 100
			&& !settings.excludeOption(position.getSymbol()))
		{
			int totalCallsSold = position.getOptions() != null ? Math.abs(
				position.getOptions().stream().filter(o -> o.getSymbol().endsWith("C") && o.getQuantity() < 0).mapToInt(Position::getQuantity).sum())
				: 0;
			int availableShares = position.getQuantity() - totalCallsSold * 100;
			availableCalls = (int)Math.floor(availableShares / 100.0);
		}
		return availableCalls;
	}

	private static List<MonthlyIncome> calculateIncome(List<? extends SchwabTransaction> transactions)
	{
		List<MonthlyIncome> monthlyIncome = new ArrayList<>();

		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM");
		Map<String, Double> monthToOptionsIncome = new HashMap<>();
		Map<String, Double> monthToDividendIncome = new HashMap<>();
		Map<String, Double> monthToContributions = new HashMap<>();
		for (SchwabTransaction transaction : transactions)
		{
			String action = transaction.action();
			if (action != null)
			{
				String dateString = transaction.date().format(format);
				if (shortOptionActions.contains(action))
					monthToOptionsIncome.merge(dateString, transaction.amount(), Double::sum);
				else if (action.contains(" Div"))
					monthToDividendIncome.merge(dateString, transaction.amount(), Double::sum);
				else if (transferActions.contains(action))
					monthToContributions.merge(dateString, transaction.amount(), Double::sum);
			}
		}

		Set<String> allMonths = new HashSet<>();
		allMonths.addAll(monthToOptionsIncome.keySet());
		allMonths.addAll(monthToDividendIncome.keySet());
		allMonths.addAll(monthToContributions.keySet());
		List<String> sortedMonths = new ArrayList<>(allMonths);
		Collections.sort(sortedMonths, Collections.reverseOrder());
		for (String month : sortedMonths)
		{
			monthlyIncome.add(new MonthlyIncome(month, monthToOptionsIncome.getOrDefault(month, 0.), monthToDividendIncome.getOrDefault(month, 0.),
				monthToContributions.getOrDefault(month, 0.)));
		}

		double optionsTotal = monthToOptionsIncome.values().stream().mapToDouble(Double::doubleValue).sum();
		double dividendTotal = monthToDividendIncome.values().stream().mapToDouble(Double::doubleValue).sum();
		double contributionsTotal = monthToContributions.values().stream().mapToDouble(Double::doubleValue).sum();
		monthlyIncome.add(new MonthlyIncome("Total", optionsTotal, dividendTotal, contributionsTotal));

		return monthlyIncome;
	}

	private static double calculateReturn(AccountSettings accountSettings, Portfolio portfolio)
	{
		final LocalDate startDate = LocalDate.of(2022, 1, 1);

		double A = accountSettings.getStartingBalance();
		double B = portfolio.getPositions().balance();
		List<SchwabTransaction> transfers = portfolio.getTransactions().transactions().stream()
			.filter(t -> transferActions.contains(t.action()) && !t.date().isBefore(startDate)).toList();
		double F = transfers.stream().mapToDouble(SchwabTransaction::amount).sum();
		double weightedF = transfers.stream().mapToDouble(t -> getWeight(t, startDate) * t.amount()).sum();
		double R = (B - A - F) / (A + weightedF);
		return R;
	}

	private static double getWeight(SchwabTransaction transaction, LocalDate startDate)
	{
		long C = 365;
		long D = ChronoUnit.DAYS.between(startDate, transaction.date());
		double W = D >= 0 ? (C - D) / (double)C : 0;
		return W;
	}

	private static double calculateCashOnHold(Collection<? extends Position> positions)
	{
		return positions.stream().filter(p -> p.isOption() && p.getSymbol().endsWith("P") && p.getQuantity() < 0)
			.mapToDouble(p -> Double.parseDouble(p.getSymbol().split(" ")[2]) * Math.abs(p.getQuantity())).sum() * 100;
	}

	private static double calculateOpenBuyAmount(SchwabOrdersData orders)
	{
		return orders != null
			? orders.getOpenOrders().stream().filter(o -> "Buy".equals(o.action()))
				.mapToDouble(o -> o.limitPrice() != null ? o.quantity() * o.limitPrice() : 0).sum()
			: 0;
	}

	private static int round(double value, double cutoff)
	{
		return (int)(value >= 0 ? value + (1 - cutoff) : value - (1 - cutoff));
	}
}
