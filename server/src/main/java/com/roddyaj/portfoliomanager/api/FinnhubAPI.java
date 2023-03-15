package com.roddyaj.portfoliomanager.api;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.portfoliomanager.model.Quote;

public class FinnhubAPI implements QuoteProvider
{
	private static final String urlRoot = "https://finnhub.io/api/v1/";

	private String apiKey;

	private int requestLimitPerMinute;

	public void setApiKey(String apiKey)
	{
		this.apiKey = apiKey;
	}

	public void setRequestLimitPerMinute(int requestLimitPerMinute)
	{
		this.requestLimitPerMinute = requestLimitPerMinute;
	}

	@Override
	public String getName()
	{
		return "Finnhub";
	}

	@Override
	public Quote getQuote(String symbol) throws IOException
	{
		JsonNode json = request(symbol, "quote", Duration.ofMinutes(15));

		double price = getDouble(json, "c");
		double previousClose = getDouble(json, "pc");
		double change = getDouble(json, "d");
		double changePct = getDouble(json, "dp");
		double open = getDouble(json, "o");
		double high = getDouble(json, "h");
		double low = getDouble(json, "l");
		long time = getLong(json, "t");

		return time != 0 ? new Quote(price, previousClose, change, changePct, open, high, low, Instant.ofEpochSecond(time)) : null;
	}

	public JsonNode getCompanyProfile2(String symbol) throws IOException
	{
		return request(symbol, "stock/profile2", Duration.ofDays(30));
	}

	private JsonNode request(String symbol, String function, Duration maxStale) throws IOException
	{
		String url = new StringBuilder(urlRoot).append(function).append("?token=").append(apiKey).append("&symbol=").append(symbol).toString();
		Response response = HttpClient.SHARED_INSTANCE.get(url, requestLimitPerMinute, maxStale);
		return new ObjectMapper().readTree(response.getBody());
	}

//	private static String getString(JsonNode obj, String key)
//	{
//		return obj.get(key).textValue();
//	}

	private static double getDouble(JsonNode obj, String key)
	{
		return obj.get(key).doubleValue();
	}

	private static long getLong(JsonNode obj, String key)
	{
		return obj.get(key).longValue();
	}
}
