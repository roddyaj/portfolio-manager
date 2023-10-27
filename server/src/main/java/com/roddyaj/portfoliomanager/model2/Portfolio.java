package com.roddyaj.portfoliomanager.model2;

import java.time.ZonedDateTime;
import java.util.List;

public record Portfolio(List<Position> positions, List<Order> openOrders, List<Order> transactions, double cash, double balance, ZonedDateTime time)
{
}
