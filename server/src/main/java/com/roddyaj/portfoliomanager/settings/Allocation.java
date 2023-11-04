package com.roddyaj.portfoliomanager.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Allocation
{
	private String cat;
	private double percent;
	private Double minOrder;
	private boolean sell = true;
	private double sellLimit;

	public Allocation()
	{
	}

	public Allocation(String cat, double percent)
	{
		this.cat = cat;
		this.percent = percent;
	}

	@JsonProperty("cat")
	public String getCat()
	{
		return cat;
	}

	@JsonProperty("cat")
	public void setCat(String cat)
	{
		this.cat = cat;
	}

	@JsonIgnore
	public String getCatLastToken()
	{
		String[] tokens = cat.split("\\.");
		return tokens[tokens.length - 1];
	}

	@JsonProperty("%")
	public double getPercent()
	{
		return percent;
	}

	@JsonProperty("%")
	public void setPercent(double percent)
	{
		this.percent = percent;
	}

	public Double getMinOrder()
	{
		return minOrder;
	}

	public void setMinOrder(Double minOrder)
	{
		this.minOrder = minOrder;
	}

	public boolean isSell()
	{
		return sell;
	}

	public void setSell(boolean sell)
	{
		this.sell = sell;
	}

	public double getSellLimit()
	{
		return sellLimit;
	}

	public void setSellLimit(double sellLimit)
	{
		this.sellLimit = sellLimit;
	}

	@Override
	public String toString()
	{
		return cat + ": " + percent;
	}
}
