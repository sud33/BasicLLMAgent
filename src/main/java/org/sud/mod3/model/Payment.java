package org.sud.mod3.model;

import java.math.BigDecimal;

/** Basic payment data available to the investigation agent. */
public record Payment(
        String paymentId,
        String customerId,
        String fundingAccountId,
        String merchantAccountId,
        BigDecimal amount,
        String currency,
        Status status,
        String failureCode,
        String createdAt)
{
    public enum Status
    {
        SUCCESS,
        FAILED,
        PENDING
    }
}
