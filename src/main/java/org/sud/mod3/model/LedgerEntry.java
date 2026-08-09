package org.sud.mod3.model;

import java.math.BigDecimal;

/** One debit, credit, or reversal recorded for a payment. */
public record LedgerEntry(
        String entryId,
        String paymentId,
        String accountId,
        Type entryType,
        Status status,
        BigDecimal amount,
        String currency,
        String timestamp)
{
    public enum Type
    {
        DEBIT,
        CREDIT,
        REVERSAL
    }

    public enum Status
    {
        POSTED,
        PENDING,
        FAILED
    }
}
