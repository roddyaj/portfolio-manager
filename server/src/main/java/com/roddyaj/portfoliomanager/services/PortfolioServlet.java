package com.roddyaj.portfoliomanager.services;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.roddyaj.portfoliomanager.logic.PortfolioManager;
import com.roddyaj.portfoliomanager.output.Output;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PortfolioServlet extends EnhancedServlet
{
	private static final long serialVersionUID = -2948464420724282868L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String accountName = request.getParameter("accountName");
		String accountNumber = request.getParameter("accountNumber");
		Path inputDir = Paths.get(System.getProperty("user.home"), "Downloads");

		Output output = new PortfolioManager().process(inputDir, accountName, accountNumber);

		writeJson(output, response);
	}
}
