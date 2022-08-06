package com.roddyaj.portfoliomanager.server;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.portfoliomanager.PortfolioManager;
import com.roddyaj.portfoliomanager.output.Output;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PortfolioServlet extends HttpServlet
{
	private static final long serialVersionUID = -2948464420724282868L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String accountName = request.getParameter("accountName");
		String accountNumber = request.getParameter("accountNumber");
		Path inputDir = Paths.get(System.getProperty("user.home"), "Downloads");

		Output output = new PortfolioManager().process(inputDir, accountName, accountNumber);

		ObjectMapper mapper = new ObjectMapper();
//		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		mapper.writeValue(response.getOutputStream(), output);
	}
}
