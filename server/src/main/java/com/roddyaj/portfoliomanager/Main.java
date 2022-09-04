package com.roddyaj.portfoliomanager;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Collection;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

import com.roddyaj.portfoliomanager.api.FinnhubAPI;
import com.roddyaj.portfoliomanager.model.Quote;
import com.roddyaj.portfoliomanager.model.State;
import com.roddyaj.portfoliomanager.services.AccountsServlet;
import com.roddyaj.portfoliomanager.services.ApplicationServlet;
import com.roddyaj.portfoliomanager.services.PortfolioServlet;
import com.roddyaj.portfoliomanager.settings.Api;
import com.roddyaj.portfoliomanager.settings.Settings;

public class Main
{
	public static void main(String[] args)
	{
		Main main = new Main();
		try
		{
			main.startServer();
			main.startQuotePolling();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private Server server;

	public void startServer() throws Exception
	{
		server = new Server();

		ServerConnector connector = new ServerConnector(server);
		connector.setHost(InetAddress.getLoopbackAddress().getHostAddress());
		connector.setPort(8090);
		server.setConnectors(new Connector[] { connector });

		ServletHandler servletHandler = new ServletHandler();
		servletHandler.addServletWithMapping(AccountsServlet.class, "/accounts");
		servletHandler.addServletWithMapping(PortfolioServlet.class, "/portfolio");
		servletHandler.addServletWithMapping(ApplicationServlet.class, "/stop-poll");
		server.setHandler(servletHandler);

		server.start();

		String url = "http://" + connector.getHost() + ":" + connector.getPort();
		System.out.println("Started server at " + url);
	}

	public void startQuotePolling()
	{
		State state = State.getInstance();
		Settings settings = state.getSettings();

		Api apiSettings;
		FinnhubAPI finnhub = new FinnhubAPI();
		apiSettings = settings.getApi(finnhub.getName());
		if (apiSettings != null)
		{
			finnhub.setApiKey(apiSettings.getApiKey());
			finnhub.setRequestLimitPerMinute(apiSettings.getRequestsPerMinute());
		}

		while (true)
		{
			Instant lastRefresh = state.getLastRefresh();
			if (lastRefresh != null && Instant.now().isBefore(lastRefresh.plusSeconds(1800)))
			{
				Collection<String> symbols = state.getSymbolsToLookup();
				for (String symbol : symbols)
				{
					try
					{
						Quote quote = finnhub.getQuote(symbol);
						if (quote != null)
							state.setQuote(symbol, quote);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}

					if (state.getLastRefresh() == null)
						break;
				}
			}

			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
