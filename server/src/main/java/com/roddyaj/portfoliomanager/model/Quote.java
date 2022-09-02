package com.roddyaj.portfoliomanager.model;

import java.time.Instant;

public record Quote(double price, double previousClose, double change, double changePct, Double open, Double high, Double low, Instant time)
{
}
