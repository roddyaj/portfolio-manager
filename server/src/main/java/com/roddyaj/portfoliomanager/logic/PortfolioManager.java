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
import com.roddyaj.portfoliomanager.api.fidelity.FidelityPortfolioReader;
import com.roddyaj.portfoliomanager.api.schwab.SchwabPortfolioReader;
import com.roddyaj.portfoliomanager.model.Message;
import com.roddyaj.portfoliomanager.model.Quote;
import com.roddyaj.portfoliomanager.model.State;
import com.roddyaj.portfoliomanager.model2.Order.TransactionType;
import com.roddyaj.portfoliomanager.model2.Portfolio;
import com.roddyaj.portfoliomanager.output.MonthlyIncome;
import com.roddyaj.portfoliomanager.output.Order;
import com.roddyaj.portfoliomanager.output.Output;
import com.roddyaj.portfoliomanager.output.Position;
import com.roddyaj.portfoliomanager.output.PutToSell;
import com.roddyaj.portfoliomanager.output.Transaction;
import com.roddyaj.portfoliomanager.settings.AccountSettings;
import com.roddyaj.portfoliomanager.settings.Allocation;
import com.roddyaj.portfoliomanager.settings.Api;
import com.roddyaj.portfoliomanager.settings.Settings;

public final class PortfolioManager
{
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

		Portfolio portfolio = accountNumber.length() == 9 ?
			new FidelityPortfolioReader().read(inputDir, accountName, accountNumber) :
			new SchwabPortfolioReader().read(inputDir, accountName, accountNumber);
		boolean anyUpdated = true;

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

		// What to filter out here?
		Map<String, List<com.roddyaj.portfoliomanager.model2.Order>> symbolToTransactions = portfolio.transactions().stream()
			.filter(t -> t.symbol() != null && t.transactionType() != null)
			.collect(Collectors.groupingBy(com.roddyaj.portfoliomanager.model2.Order::symbol));

		// What to filter out here?
		Map<String, List<com.roddyaj.portfoliomanager.model2.Order>> symbolToOrders = portfolio.openOrders().stream()
			.collect(Collectors.groupingBy(com.roddyaj.portfoliomanager.model2.Order::symbol));

		List<Message> messages = new ArrayList<>();
		AllocationMap allocationMap = new AllocationMap(accountSettings.getAllocations(), messages);

		State state = State.getInstance();

		Instant portfolioTime = portfolio.time().toInstant();

		Map<String, List<com.roddyaj.portfoliomanager.model2.Position>> symbolToOptions = portfolio.positions().stream()
			.filter(com.roddyaj.portfoliomanager.model2.Position::isOption)
			.collect(Collectors.groupingBy(com.roddyaj.portfoliomanager.model2.Position::symbol));
		Map<String, com.roddyaj.portfoliomanager.model2.Position> symbolToPosition = portfolio.positions().stream().filter(p -> !p.isOption())
			.collect(Collectors.toMap(com.roddyaj.portfoliomanager.model2.Position::symbol, Function.identity()));

		output.setBalance(portfolio.balance());
		output.setCash(portfolio.cash());
		output.setPortfolioReturn(calculateReturn(accountSettings, portfolio));
		output.setPositionsTime(portfolio.time().toEpochSecond() * 1000);

		List<Position> allPositions = new ArrayList<>();
		allPositions.addAll(portfolio.positions().stream().map(PortfolioManager::toPosition).toList());
		allPositions.addAll(getNewPositions(portfolio, allocationMap));
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
			position.setSharesToBuy(calculateSharesToBuy(position, accountSettings, portfolio.balance(), target));
			position.setCallsToSell(calculateCallsToSell(position, settings, accountSettings));

			output.getPositions().add(position);
		}

		output.setCashOnHold(calculateCashOnHold(allPositions));
		output.setOpenBuyAmount(calculateOpenBuyAmount(portfolio.openOrders()));
		output.setCashAvailable(output.getCash() - output.getCashOnHold() - output.getOpenBuyAmount());

		if (accountSettings.isOptionsEnabled())
		{
			output.setPutsToSell(Stream.of(settings.getOptionsInclude()).map(s -> {
				Double price = null;
				Double dayChange = null;
				try
				{
					Quote quote;
					if (symbolToPosition.containsKey(s))
					{
						price = symbolToPosition.get(s).price();
						dayChange = symbolToPosition.get(s).dayChangePct();
					}
					else if ((quote = finnhubAPI.getQuote(s)) != null)
					{
						price = quote.price();
						dayChange = quote.changePct();
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				return new PutToSell(s, price, dayChange);
			}).toList());
		}

		List<String> allSymbols = allPositions.stream().filter(p -> !p.isOption()).map(Position::getSymbol).toList();
		state.setSymbolsToLookup(allSymbols);

		output.setIncome(calculateIncome(portfolio.transactions()));

		state.setLastRefresh(Instant.now());

		output.setSp500YtdReturn(SP500ReturnAPI.getYtdSp500Return());

		return output;
	}

	private static Position toPosition(com.roddyaj.portfoliomanager.model2.Position inputPosition)
	{
		Position position = new Position();
		position.setSymbol(inputPosition.symbol());
		position.setDescription(inputPosition.description());
		position.setQuantity((int)inputPosition.quantity());
		position.setPrice(inputPosition.price());
		position.setMarketValue(inputPosition.getMarketValue());
		position.setCostBasis(inputPosition.costBasis());
		position.setDayChangePct(inputPosition.dayChangePct());
		position.setGainLossPct(inputPosition.getGainLossPct());
		position.setPercentOfAccount(inputPosition.percentOfAccount());
		if (inputPosition.isOption())
		{
			position.setUnderlyingPrice(inputPosition.option().getUnderlyingPrice());
			position.setInTheMoney(inputPosition.option().inTheMoney());
			position.setDte((int)ChronoUnit.DAYS.between(LocalDate.now(), inputPosition.option().expiryDate()));
			position.setOptionExpiry(inputPosition.option().expiryDate().toString());
			position.setOptionType(inputPosition.option().type().toString());
			position.setOptionStrike(inputPosition.option().strike());
			position.setOption(true);
		}
		return position;
	}

	private static Transaction toTransaction(com.roddyaj.portfoliomanager.model2.Order schwabTransaction)
	{
		Transaction transaction = new Transaction();
		transaction.setDate(schwabTransaction.date().toString());
		transaction.setAction(schwabTransaction.transactionType().toString());
		transaction.setQuantity(schwabTransaction.quantity());
		transaction.setPrice(schwabTransaction.price());
		transaction.setAmount(schwabTransaction.getAmount());
		if (schwabTransaction.isOption())
		{
			transaction.setStrike(schwabTransaction.option().strike());
			transaction.setType(schwabTransaction.option().type().toString().substring(0, 1));
		}
		return transaction;
	}

	private static Order toOrder(com.roddyaj.portfoliomanager.model2.Order schwabOrder)
	{
		Order order = new Order();
		order.setAction(schwabOrder.transactionType().toString());
		order.setQuantity((int)schwabOrder.quantity());
		order.setOrderType(schwabOrder.orderType().toString());
		order.setLimitPrice(schwabOrder.price());
//		order.setTiming(schwabOrder.timing().toString());
		if (schwabOrder.isOption())
		{
			order.setStrike(schwabOrder.option().strike());
			order.setType(schwabOrder.option().type().toString().substring(0, 1));
		}
		return order;
	}

	private List<Position> getNewPositions(Portfolio portfolio, AllocationMap allocationMap)
	{
		List<Position> newPositions = new ArrayList<>();

		Set<String> positionSymbols = portfolio.positions().stream().map(com.roddyaj.portfoliomanager.model2.Position::symbol)
			.collect(Collectors.toSet());
		Set<String> buySymbols = portfolio.openOrders().stream().filter(o -> o.transactionType() == TransactionType.BUY)
			.map(com.roddyaj.portfoliomanager.model2.Order::symbol).collect(Collectors.toSet());
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
			int quantity = round((targetValue - position.getMarketValue()) / position.getPrice(), .75);
			double orderAmount = Math.abs(quantity * position.getPrice());
			Allocation allocation = accountSettings.getAllocation(position.getSymbol());
			double positionMinOrder = allocation != null && allocation.getMinOrder() != null ? allocation.getMinOrder().doubleValue() : 0;
			double valuePct = position.getMarketValue() / targetValue;
			boolean doOrder = quantity != 0 && (valuePct < .99 || valuePct > 1.01) && orderAmount >= accountSettings.getMinOrder()
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

	private static List<MonthlyIncome> calculateIncome(Collection<? extends com.roddyaj.portfoliomanager.model2.Order> transactions)
	{
		List<MonthlyIncome> monthlyIncome = new ArrayList<>();

		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM");
		Map<String, Double> monthToOptionsIncome = new HashMap<>();
		Map<String, Double> monthToDividendIncome = new HashMap<>();
		Map<String, Double> monthToContributions = new HashMap<>();
		for (com.roddyaj.portfoliomanager.model2.Order transaction : transactions)
		{
			TransactionType action = transaction.transactionType();
			if (action != null)
			{
				String dateString = transaction.date().format(format);
				if (action == TransactionType.SELL_TO_OPEN || action == TransactionType.BUY_TO_CLOSE)
					monthToOptionsIncome.merge(dateString, transaction.getAmount(), Double::sum);
				else if (action == TransactionType.DIVIDEND)
					monthToDividendIncome.merge(dateString, transaction.getAmount(), Double::sum);
				else if (action == TransactionType.TRANSFER)
					monthToContributions.merge(dateString, transaction.getAmount(), Double::sum);
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
		if (portfolio.transactions().isEmpty())
			return 0;

		final LocalDate startDate = LocalDate.of(2023, 1, 1);

		double A = accountSettings.getStartingBalance();
		double B = portfolio.balance();
		List<com.roddyaj.portfoliomanager.model2.Order> transfers = portfolio.transactions().stream()
			.filter(t -> t.transactionType() == TransactionType.TRANSFER && !t.date().isBefore(startDate)).toList();
		double F = transfers.stream().mapToDouble(com.roddyaj.portfoliomanager.model2.Order::getAmount).sum();
		double weightedF = transfers.stream().mapToDouble(t -> getWeight(t, startDate) * t.getAmount()).sum();
		double R = (B - A - F) / (A + weightedF);
		return R;
	}

	private static double getWeight(com.roddyaj.portfoliomanager.model2.Order transaction, LocalDate startDate)
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

	private static double calculateOpenBuyAmount(Collection<? extends com.roddyaj.portfoliomanager.model2.Order> orders)
	{
		return orders.stream().filter(o -> o.transactionType() == TransactionType.BUY).mapToDouble(o -> o.getAmount()).sum();
	}

	private static int round(double value, double cutoff)
	{
		return (int)(value >= 0 ? value + (1 - cutoff) : value - (1 - cutoff));
	}
}
