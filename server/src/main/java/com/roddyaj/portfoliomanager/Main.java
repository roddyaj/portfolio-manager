package com.roddyaj.portfoliomanager;

import java.net.InetAddress;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

import com.roddyaj.portfoliomanager.services.AccountsServlet;
import com.roddyaj.portfoliomanager.services.PortfolioServlet;

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

		String url = "http://" + connector.getHost() + ":" + connector.getPort();
		System.out.println("Started server at " + url);
	}
}
