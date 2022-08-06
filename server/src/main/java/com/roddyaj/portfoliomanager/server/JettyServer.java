package com.roddyaj.portfoliomanager.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

public class JettyServer
{
	public static void main(String[] args)
	{
		JettyServer jettyServer = new JettyServer();
		try
		{
			jettyServer.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private Server server;

	public void start() throws Exception
	{
		server = new Server();

		ServerConnector connector = new ServerConnector(server);
		connector.setPort(8090);
		server.setConnectors(new Connector[] { connector });

		ServletHandler servletHandler = new ServletHandler();
		servletHandler.addServletWithMapping(PortfolioServlet.class, "/portfolio");
		server.setHandler(servletHandler);

		server.start();
	}
}
