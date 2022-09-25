package com.roddyaj.portfoliomanager.api;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SP500ReturnAPI
{
	public static double getYtdSp500Return()
	{
		double ytdReturn = 0;
		final String url = "https://www.slickcharts.com/sp500/returns/ytd";
		try
		{
			Response response = HttpClient.SHARED_INSTANCE.get(url, null, Duration.ofHours(4));
			String[] lines = response.getBody().split("\n");
			List<String> tds = Stream.of(lines).filter(line -> line.contains("<td")).toList();
			if (tds.size() > 1)
			{
				final Pattern pattern = Pattern.compile("<td.*>(.+?)</td>");
				Matcher matcher = pattern.matcher(tds.get(1));
				if (matcher.find())
					ytdReturn = Double.parseDouble(matcher.group(1));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return ytdReturn;
	}
}
