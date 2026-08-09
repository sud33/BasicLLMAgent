package org.sud.mod3.repository;

import org.junit.jupiter.api.Test;
import org.sud.mod3.model.LedgerEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LedgerRepositoryTest
{
    private final LedgerRepository repository = new LedgerRepository();

    @Test
    void returnsLedgerEntriesForKnownPayment()
    {
        List<LedgerEntry> entries = repository.findByPaymentId("PAY-2001");

        assertEquals(2, entries.size());
        assertEquals(LedgerEntry.Status.POSTED, entries.get(0).status());
        assertEquals(LedgerEntry.Status.PENDING, entries.get(1).status());
    }

    @Test
    void returnsEmptyListWhenPaymentHasNoLedgerEntries()
    {
        assertTrue(repository.findByPaymentId("PAY-9999").isEmpty());
    }
}
