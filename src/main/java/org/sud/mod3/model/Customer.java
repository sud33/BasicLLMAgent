package org.sud.mod3.model;

/** Customer data available when an investigation needs customer context. */
public record Customer(
        String customerId,
        String name,
        Status status,
        RiskLevel riskLevel)
{
    public enum Status
    {
        ACTIVE,
        SUSPENDED,
        CLOSED
    }

    public enum RiskLevel
    {
        LOW,
        MEDIUM,
        HIGH
    }
}
