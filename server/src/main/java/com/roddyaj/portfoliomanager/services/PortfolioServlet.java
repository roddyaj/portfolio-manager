package com.roddyaj.portfoliomanager.services;

import java.io.IOException;
import java.util.stream.Stream;

import com.roddyaj.portfoliomanager.logic.PortfolioManager;
import com.roddyaj.portfoliomanager.model.State;
import com.roddyaj.portfoliomanager.output.Output;
import com.roddyaj.portfoliomanager.settings.AccountSettings;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PortfolioServlet extends EnhancedServlet
{
	private static final long serialVersionUID = -2948464420724282868L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		State state = State.getInstance();

		String accountName = request.getParameter("accountName");
		String accountNumber = Stream.of(state.getSettings().getAccounts()).filter(a -> a.getName().equals(accountName))
			.map(AccountSettings::getAccountNumber).findAny().orElse(null);
		AccountSettings accountSettings = state.getSettings().getAccount(accountName);

		Output output = new PortfolioManager().process(state.getInputDir(), accountName, accountNumber, accountSettings);

		writeJson(output, response);
	}
}