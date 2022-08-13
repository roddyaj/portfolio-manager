package com.roddyaj.portfoliomanager;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.portfoliomanager.model.State;
import com.roddyaj.portfoliomanager.services.AccountsServlet;
import com.roddyaj.portfoliomanager.services.PortfolioServlet;
import com.roddyaj.portfoliomanager.settings.Settings;

public class Main
{
	public static void main(String[] args)
	{
		Main main = new Main();
		try
		{
			main.startServer();
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
		server.setHandler(servletHandler);

		server.start();

		readSettings();

		String url = "http://" + connector.getHost() + ":" + connector.getPort();
		System.out.println("Started server at " + url);
	}

	private void readSettings()
	{
		State state = State.getInstance();

		Path settingsFile = Paths.get(Paths.get(System.getProperty("user.home"), ".invest").toString(), "settings.json");
		try
		{
			Settings settings = new ObjectMapper().readValue(settingsFile.toFile(), Settings.class);
			state.setSettings(settings);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
