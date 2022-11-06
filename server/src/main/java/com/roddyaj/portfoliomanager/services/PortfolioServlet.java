package com.roddyaj.portfoliomanager.services;

import java.io.IOException;

import com.roddyaj.portfoliomanager.logic.PortfolioManager;
import com.roddyaj.portfoliomanager.model.State;
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
		try
		{
			State state = State.getInstance();

			String accountName = request.getParameter("accountName");

			Output output = new PortfolioManager(state.getSettings()).process(state.getInputDir(), accountName, state.getSettings());

			writeJson(output, response);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			writeError(e.getMessage(), response);
		}
	}
}
