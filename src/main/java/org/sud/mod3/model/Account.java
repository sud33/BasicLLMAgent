package org.sud.mod3.model;

/** Account data that may explain payment validation or posting problems. */
public record Account(
        String accountId,
        String customerId,
        Type accountType,
        Status status,
        String closedDate)
{
    public enum Type
    {
        CUSTOMER,
        MERCHANT
    }

    public enum Status
    {
        ACTIVE,
        CLOSED,
        BLOCKED
    }
}
