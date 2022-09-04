package com.roddyaj.portfoliomanager.services;

import java.io.IOException;

import com.roddyaj.portfoliomanager.model.State;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApplicationServlet extends EnhancedServlet
{
	private static final long serialVersionUID = 4313809456691395087L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		State state = State.getInstance();

		state.setLastRefresh(null);

		writeJson("stopped", response);
	}
}
