package org.sud.mod3.repository;

import org.junit.jupiter.api.Test;
import org.sud.mod3.model.Payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentRepositoryTest
{
    private final PaymentRepository repository = new PaymentRepository();

    @Test
    void findsKnownPayment()
    {
        Payment payment = repository.findById("PAY-1004").orElseThrow();

        assertEquals(Payment.Status.FAILED, payment.status());
        assertEquals("ACC-500", payment.fundingAccountId());
        assertEquals("ACCOUNT_VALIDATION_FAILED", payment.failureCode());
    }

    @Test
    void returnsEmptyForUnknownPayment()
    {
        assertTrue(repository.findById("PAY-9999").isEmpty());
    }
}
