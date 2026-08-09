package org.sud.mod3.repository;

import org.sud.mod3.model.Payment;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/** In-memory payment data used by the example tools. */
public class PaymentRepository
{
    private final Map<String, Payment> payments = Map.of(
            "PAY-1004", new Payment("PAY-1004", "CUST-101", "ACC-500", "MERCHANT-20",
                    new BigDecimal("850.00"), "CAD", Payment.Status.FAILED,
                    "ACCOUNT_VALIDATION_FAILED", "2026-08-01T14:10:00Z"),
            "PAY-2001", new Payment("PAY-2001", "CUST-202", "ACC-600", "MERCHANT-30",
                    new BigDecimal("125.00"), "CAD", Payment.Status.SUCCESS,
                    null, "2026-08-02T09:15:00Z"),
            "PAY-3005", new Payment("PAY-3005", "CUST-303", "ACC-700", "MERCHANT-40",
                    new BigDecimal("45.00"), "CAD", Payment.Status.SUCCESS,
                    null, "2026-08-03T11:30:00Z"),
            "PAY-4002", new Payment("PAY-4002", "CUST-404", "ACC-800", "MERCHANT-20",
                    new BigDecimal("300.00"), "CAD", Payment.Status.FAILED,
                    "CUSTOMER_VALIDATION_FAILED", "2026-08-04T16:45:00Z")
    );

    public Optional<Payment> findById(String paymentId)
    {
        return Optional.ofNullable(payments.get(paymentId));
    }
}
