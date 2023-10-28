package com.roddyaj.portfoliomanager.services;

import java.io.IOException;
import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

public abstract class EnhancedServlet extends HttpServlet
{
	private static final long serialVersionUID = 4076879779038204731L;

	protected static final ObjectMapper MAPPER = JsonMapper.builder().addModule(new JavaTimeModule()).build();

	public void writeJson(Object value, HttpServletResponse response) throws IOException
	{
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		response.addHeader("Access-Control-Allow-Origin", "*");

		MAPPER.writeValue(response.getOutputStream(), value);
	}

	public void writeError(String message, HttpServletResponse response) throws IOException
	{
		response.setContentType("text/plain");
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		response.addHeader("Access-Control-Allow-Origin", "*");

		response.getOutputStream().write(message.getBytes(Charset.defaultCharset()));
	}
}
