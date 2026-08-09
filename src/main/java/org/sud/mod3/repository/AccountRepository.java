package org.sud.mod3.repository;

import org.sud.mod3.model.Account;

import java.util.Map;
import java.util.Optional;

/** In-memory customer and merchant account data used by the example tools. */
public class AccountRepository
{
    private final Map<String, Account> accounts = Map.of(
            "ACC-500", new Account("ACC-500", "CUST-101", Account.Type.CUSTOMER,
                    Account.Status.CLOSED, "2026-07-31"),
            "ACC-600", new Account("ACC-600", "CUST-202", Account.Type.CUSTOMER,
                    Account.Status.ACTIVE, null),
            "MERCHANT-30", new Account("MERCHANT-30", "CUST-M30", Account.Type.MERCHANT,
                    Account.Status.ACTIVE, null),
            "ACC-700", new Account("ACC-700", "CUST-303", Account.Type.CUSTOMER,
                    Account.Status.ACTIVE, null),
            "MERCHANT-40", new Account("MERCHANT-40", "CUST-M40", Account.Type.MERCHANT,
                    Account.Status.ACTIVE, null),
            "ACC-800", new Account("ACC-800", "CUST-404", Account.Type.CUSTOMER,
                    Account.Status.ACTIVE, null),
            "MERCHANT-20", new Account("MERCHANT-20", "CUST-M20", Account.Type.MERCHANT,
                    Account.Status.ACTIVE, null)
    );

    public Optional<Account> findById(String accountId)
    {
        return Optional.ofNullable(accounts.get(accountId));
    }
}
