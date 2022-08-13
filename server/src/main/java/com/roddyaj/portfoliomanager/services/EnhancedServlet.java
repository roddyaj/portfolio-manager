package com.roddyaj.portfoliomanager.services;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

public abstract class EnhancedServlet extends HttpServlet
{
	private static final long serialVersionUID = 4076879779038204731L;

	protected static final ObjectMapper MAPPER = new ObjectMapper();

	public void writeJson(Object value, HttpServletResponse response) throws IOException
	{
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		response.addHeader("Access-Control-Allow-Origin", "*");

		MAPPER.writeValue(response.getOutputStream(), value);
	}
}
