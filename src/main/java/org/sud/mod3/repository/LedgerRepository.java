package org.sud.mod3.repository;

import org.sud.mod3.model.LedgerEntry;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** In-memory ledger activity grouped by payment ID. */
public class LedgerRepository
{
    private final Map<String, List<LedgerEntry>> entriesByPayment = Map.of(
            "PAY-2001", List.of(
                    new LedgerEntry("LEDGER-2001-D", "PAY-2001", "ACC-600",
                            LedgerEntry.Type.DEBIT, LedgerEntry.Status.POSTED,
                            new BigDecimal("125.00"), "CAD", "2026-08-02T09:15:02Z"),
                    new LedgerEntry("LEDGER-2001-C", "PAY-2001", "MERCHANT-30",
                            LedgerEntry.Type.CREDIT, LedgerEntry.Status.PENDING,
                            new BigDecimal("125.00"), "CAD", "2026-08-02T09:15:03Z")),
            "PAY-3005", List.of(
                    new LedgerEntry("LEDGER-3005-D", "PAY-3005", "ACC-700",
                            LedgerEntry.Type.DEBIT, LedgerEntry.Status.POSTED,
                            new BigDecimal("45.00"), "CAD", "2026-08-03T11:30:02Z"),
                    new LedgerEntry("LEDGER-3005-C", "PAY-3005", "MERCHANT-40",
                            LedgerEntry.Type.CREDIT, LedgerEntry.Status.POSTED,
                            new BigDecimal("45.00"), "CAD", "2026-08-03T11:30:03Z"))
    );

    public List<LedgerEntry> findByPaymentId(String paymentId)
    {
        return entriesByPayment.getOrDefault(paymentId, List.of());
    }
}
