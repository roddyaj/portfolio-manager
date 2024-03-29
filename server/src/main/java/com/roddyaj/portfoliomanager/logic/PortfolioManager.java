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

import com.roddyaj.portfoliomanager.api.FinnhubAPI;
import com.roddyaj.portfoliomanager.api.PortfolioReader;
import com.roddyaj.portfoliomanager.api.fidelity.FidelityPortfolioReader;
import com.roddyaj.portfoliomanager.api.schwab.SchwabPortfolioReader;
import com.roddyaj.portfoliomanager.model.Message;
import com.roddyaj.portfoliomanager.model.Option.Type;
import com.roddyaj.portfoliomanager.model.Order;
import com.roddyaj.portfoliomanager.model.Order.TransactionType;
import com.roddyaj.portfoliomanager.model.Portfolio;
import com.roddyaj.portfoliomanager.model.Position;
import com.roddyaj.portfoliomanager.model.Quote;
import com.roddyaj.portfoliomanager.model.State;
import com.roddyaj.portfoliomanager.output.MonthlyIncome;
import com.roddyaj.portfoliomanager.output.Output;
import com.roddyaj.portfoliomanager.output.OutputPosition;
import com.roddyaj.portfoliomanager.output.PutToSell;
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

		PortfolioReader reader = accountNumber.length() == 9 ? new FidelityPortfolioReader() : new SchwabPortfolioReader();
		Portfolio portfolio = reader.read(inputDir, accountName, accountNumber);

		Output output = createOutput(accountName, settings, accountSettings, portfolio);
		return output;
	}

	private Output createOutput(String accountName, Settings settings, AccountSettings accountSettings, Portfolio portfolio)
	{
		Output output = new Output();
		output.setAccountName(accountName);
		output.setOptionsEnabled(accountSettings.isOptionsEnabled());

		Map<String, List<Order>> symbolToTransactions = portfolio.transactions().stream()
			.filter(t -> t.symbol() != null && t.transactionType() != null).collect(Collectors.groupingBy(Order::symbol));

		Map<String, List<Order>> symbolToOrders = portfolio.openOrders().stream().collect(Collectors.groupingBy(Order::symbol));

		List<Message> messages = new ArrayList<>();
		AllocationMap allocationMap = new AllocationMap(accountSettings.getAllocations(), messages);

		State state = State.getInstance();

		Instant portfolioTime = portfolio.time().toInstant();

		Map<String, List<Position>> symbolToOptions = portfolio.positions().stream().filter(p -> p.option() != null)
			.collect(Collectors.groupingBy(Position::symbol));
		Map<String, Position> symbolToPosition = portfolio.positions().stream().filter(p -> p.option() == null)
			.collect(Collectors.toMap(Position::symbol, Function.identity()));

		output.setBalance(portfolio.balance());
		output.setCash(portfolio.cash());
//		output.setPortfolioReturn(calculateReturn(accountSettings, portfolio));
		output.setPositionsTime(portfolio.time().toEpochSecond() * 1000);

		List<OutputPosition> allPositions = new ArrayList<>();
		allPositions.addAll(portfolio.positions().stream().map(PortfolioManager::toPosition).toList());
		allPositions.addAll(getNewPositions(portfolio, allocationMap));
		for (OutputPosition position : allPositions)
		{
			String symbol = position.getSymbol();
			position.setTransactions(symbolToTransactions.getOrDefault(symbol, List.of()));
			position.setOpenOrders(symbolToOrders.getOrDefault(symbol, List.of()));
			position.setOptions(symbolToOptions.getOrDefault(symbol, List.of()));

			Quote quote = state.getQuote(symbol);
			if (quote != null && (quote.time().isAfter(portfolioTime) || position.getQuantity() == 0))
			{
				double marketValue = quote.price() * position.getQuantity();
				double gainLossPct = position.getCostBasis() > 0 ? (marketValue / position.getCostBasis() - 1) * 100 : 0;
				position.setPrice(quote.price());
				position.setMarketValue(marketValue);
				position.setDayChangePct(quote.changePct());
				position.setGainLossPct(gainLossPct);
			}

//			JsonNode companyInfo = state.getCompanyInfo(symbol);
//			if (companyInfo != null)
//				position.setLogo(companyInfo.get("logo").textValue());

			Double target = allocationMap.getAllocation(symbol);
			position.setTargetPct(target != null ? (target.doubleValue() * 100) : null);
			position.setSharesToBuy(calculateSharesToBuy(position, accountSettings, portfolio.balance(), target));
			position.setCallsToSell(calculateCallsToSell(position, settings, accountSettings));

			output.getPositions().add(position);
		}

		output.setCashOnHold(calculateCashOnHold(allPositions));
		output.setOpenBuyAmount(calculateOpenBuyAmount(portfolio.openOrders()));
		output.setCashAvailable(output.getCash() - output.getCashOnHold() - output.getOpenBuyAmount());

		final Set<String> btcTickers = Set.of("BITO", "GBTC");
		output.setBtcBalance(portfolio.positions().stream().filter(p -> p.option() == null && btcTickers.contains(p.symbol()))
			.mapToDouble(Position::getMarketValue).sum());
		output.setBtcPrice(finnhubAPI.getPrice("COINBASE:BTC-USD", 0));

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

		List<String> allSymbols = allPositions.stream().filter(p -> !p.isOption()).map(OutputPosition::getSymbol).toList();
		state.setSymbolsToLookup(allSymbols);

		output.setIncome(calculateIncome(portfolio.transactions()));

		state.setLastRefresh(Instant.now());

//		output.setSp500YtdReturn(SP500ReturnAPI.getYtdSp500Return());

		return output;
	}

	private static OutputPosition toPosition(Position inputPosition)
	{
		OutputPosition position = new OutputPosition();
		position.setSymbol(inputPosition.symbol());
		position.setDescription(inputPosition.description());
		position.setQuantity((int)inputPosition.quantity());
		position.setPrice(inputPosition.price());
		position.setMarketValue(inputPosition.getMarketValue());
		position.setCostBasis(inputPosition.costBasis());
		position.setDayChangePct(inputPosition.dayChangePct());
		position.setGainLossPct(inputPosition.getGainLossPct());
		position.setPercentOfAccount(inputPosition.percentOfAccount());
		if (inputPosition.option() != null)
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

	private List<OutputPosition> getNewPositions(Portfolio portfolio, AllocationMap allocationMap)
	{
		List<OutputPosition> newPositions = new ArrayList<>();

		Set<String> positionSymbols = portfolio.positions().stream().map(Position::symbol).collect(Collectors.toSet());
		Set<String> buySymbols = portfolio.openOrders().stream().filter(o -> o.transactionType() == TransactionType.BUY).map(Order::symbol)
			.collect(Collectors.toSet());
		Set<String> newSymbols = new HashSet<>();
		newSymbols.addAll(allocationMap.getSymbols());
		newSymbols.addAll(buySymbols);
		newSymbols.removeAll(positionSymbols);

		for (String symbol : newSymbols)
		{
			OutputPosition position = new OutputPosition();
			position.setSymbol(symbol);
			position.setPrice(finnhubAPI.getPrice(symbol, 1));
			newPositions.add(position);
		}

		return newPositions;
	}

	private static Integer calculateSharesToBuy(OutputPosition position, AccountSettings accountSettings, double accountBalance, Double target)
	{
		Integer sharesToBuy = null;
		if (target != null)
		{
			double targetValue = accountBalance * target.doubleValue();
			int quantity = round((targetValue - position.getMarketValue()) / position.getPrice(), .75);
			double orderAmount = Math.abs(quantity * position.getPrice());
			Allocation allocation = accountSettings.getAllocation(position.getSymbol());
			double positionMinOrder = allocation != null && allocation.getMinOrder() != null ? allocation.getMinOrder().doubleValue() : 0;
			boolean allowSell = allocation == null || allocation.isSell();
			double sellLimit = allocation == null ? 0 : allocation.getSellLimit();
			double valuePct = position.getMarketValue() / targetValue;
			boolean doOrder = quantity != 0 && (valuePct < .99 || valuePct > 1.01) && orderAmount >= accountSettings.getMinOrder()
				&& orderAmount >= positionMinOrder && (quantity > 0 || (allowSell && position.getPrice() >= sellLimit));
			if (doOrder)
				sharesToBuy = quantity;
		}
		return sharesToBuy;
	}

	private static Integer calculateCallsToSell(OutputPosition position, Settings settings, AccountSettings accountSettings)
	{
		Integer availableCalls = null;
		if (accountSettings.isOptionsEnabled() && !position.isOption() && position.getQuantity() >= 100
			&& !settings.excludeOption(position.getSymbol()))
		{
			int totalCallsSold = position.getOptions() != null
				? Math.abs(position.getOptions().stream().filter(o -> o.option().type() == Type.CALL && o.quantity() < 0)
					.mapToInt(o -> (int)o.quantity()).sum())
				: 0;
			int availableShares = position.getQuantity() - totalCallsSold * 100;
			availableCalls = (int)Math.floor(availableShares / 100.0);
		}
		return availableCalls;
	}

	private static List<MonthlyIncome> calculateIncome(Collection<? extends Order> transactions)
	{
		List<MonthlyIncome> monthlyIncome = new ArrayList<>();

		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM");
		Map<String, Double> monthToOptionsIncome = new HashMap<>();
		Map<String, Double> monthToDividendIncome = new HashMap<>();
		Map<String, Double> monthToContributions = new HashMap<>();
		for (Order transaction : transactions)
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

//	private static double calculateReturn(AccountSettings accountSettings, Portfolio portfolio)
//	{
//		if (portfolio.transactions().isEmpty())
//			return 0;
//
//		final LocalDate startDate = LocalDate.of(2023, 1, 1);
//
//		double A = accountSettings.getStartingBalance();
//		double B = portfolio.balance();
//		List<Order> transfers = portfolio.transactions().stream()
//			.filter(t -> t.transactionType() == TransactionType.TRANSFER && !t.date().isBefore(startDate)).toList();
//		double F = transfers.stream().mapToDouble(Order::getAmount).sum();
//		double weightedF = transfers.stream().mapToDouble(t -> getWeight(t, startDate) * t.getAmount()).sum();
//		double R = (B - A - F) / (A + weightedF);
//		return R;
//	}
//
//	private static double getWeight(Order transaction, LocalDate startDate)
//	{
//		long C = 365;
//		long D = ChronoUnit.DAYS.between(startDate, transaction.date());
//		double W = D >= 0 ? (C - D) / (double)C : 0;
//		return W;
//	}

	private static double calculateCashOnHold(Collection<? extends OutputPosition> positions)
	{
		return positions.stream().filter(p -> p.isOption() && p.getOptionType().equals("PUT") && p.getQuantity() < 0)
			.mapToDouble(p -> p.getOptionStrike().doubleValue() * Math.abs(p.getQuantity())).sum() * 100;
	}

	private static double calculateOpenBuyAmount(Collection<? extends Order> orders)
	{
		return orders.stream().filter(o -> o.transactionType() == TransactionType.BUY).mapToDouble(o -> o.getAmount()).sum();
	}

	private static int round(double value, double cutoff)
	{
		return (int)(value >= 0 ? value + (1 - cutoff) : value - (1 - cutoff));
	}
}
