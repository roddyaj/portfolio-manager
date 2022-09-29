package com.roddyaj.portfoliomanager.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Order
{
	private String action;
	private int quantity;
	private String orderType;
	private Double limitPrice;
	private String timing;
	private Double strike;
	private String type;

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}

	public String getOrderType()
	{
		return orderType;
	}

	public void setOrderType(String orderType)
	{
		this.orderType = orderType;
	}

	public Double getLimitPrice()
	{
		return limitPrice;
	}

	public void setLimitPrice(Double limitPrice)
	{
		this.limitPrice = limitPrice;
	}

	public String getTiming()
	{
		return timing;
	}

	public void setTiming(String timing)
	{
		this.timing = timing;
	}

	public Double getStrike()
	{
		return strike;
	}

	public void setStrike(Double strike)
	{
		this.strike = strike;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}
}
