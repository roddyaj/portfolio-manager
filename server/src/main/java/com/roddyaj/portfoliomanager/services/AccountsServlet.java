package com.roddyaj.portfoliomanager.services;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.roddyaj.portfoliomanager.model.State;
import com.roddyaj.portfoliomanager.settings.AccountSettings;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AccountsServlet extends EnhancedServlet
{
	private static final long serialVersionUID = 2261892399847342924L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		State state = State.getInstance();

		List<String> accountNames = Stream.of(state.getSettings().getAccounts()).map(AccountSettings::getName).toList();
		ArrayNode accounts = MAPPER.createArrayNode();
		for (String accountName : accountNames)
			accounts.add(accountName);

		writeJson(accounts, response);
	}
}
