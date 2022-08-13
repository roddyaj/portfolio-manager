package com.roddyaj.portfoliomanager.services;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.ArrayNode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AccountsServlet extends EnhancedServlet
{
	private static final long serialVersionUID = 2261892399847342924L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		ArrayNode accounts = MAPPER.createArrayNode();
		accounts.add("PCRA");
		accounts.add("other");

		writeJson(accounts, response);
	}
}
