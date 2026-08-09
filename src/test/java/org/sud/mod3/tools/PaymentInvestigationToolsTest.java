package org.sud.mod3.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sud.mod3.repository.AccountRepository;
import org.sud.mod3.repository.CustomerRepository;
import org.sud.mod3.repository.LedgerRepository;
import org.sud.mod3.repository.PaymentRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentInvestigationToolsTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();
    private PaymentInvestigationTools tools;

    @BeforeEach
    void setUp()
    {
        tools = new PaymentInvestigationTools(
                new PaymentRepository(),
                new AccountRepository(),
                new CustomerRepository(),
                new LedgerRepository());
    }

    @Test
    void mapsPaymentIdArgumentToPaymentRepository() throws Exception
    {
        JsonNode result = json(tools.execute(
                PaymentInvestigationTools.GET_PAYMENT,
                "{\"paymentId\":\"PAY-1004\"}"));

        assertTrue(result.get("found").asBoolean());
        assertEquals("FAILED", result.at("/payment/status").asText());
        assertEquals("ACC-500", result.at("/payment/fundingAccountId").asText());
    }

    @Test
    void mapsAccountAndCustomerArgumentsToTheirRepositories() throws Exception
    {
        JsonNode account = json(tools.execute(
                PaymentInvestigationTools.GET_ACCOUNT,
                "{\"accountId\":\"ACC-500\"}"));
        JsonNode customer = json(tools.execute(
                PaymentInvestigationTools.GET_CUSTOMER,
                "{\"customerId\":\"CUST-404\"}"));

        assertEquals("CLOSED", account.at("/account/status").asText());
        assertEquals("2026-07-31", account.at("/account/closedDate").asText());
        assertEquals("SUSPENDED", customer.at("/customer/status").asText());
    }

    @Test
    void mapsPaymentIdArgumentToLedgerRepository() throws Exception
    {
        JsonNode result = json(tools.execute(
                PaymentInvestigationTools.GET_LEDGER_ENTRIES,
                "{\"paymentId\":\"PAY-2001\"}"));

        assertEquals(2, result.get("entries").size());
        assertEquals("PENDING", result.at("/entries/1/status").asText());
    }

    @Test
    void returnsClearNotFoundObservationForUnknownPayment() throws Exception
    {
        JsonNode result = json(tools.execute(
                PaymentInvestigationTools.GET_PAYMENT,
                "{\"paymentId\":\"PAY-9999\"}"));

        assertFalse(result.get("found").asBoolean());
        assertTrue(result.get("message").asText().contains("No payment"));
    }

    @Test
    void rejectsMissingRequiredArgument()
    {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tools.execute(PaymentInvestigationTools.GET_ACCOUNT, "{}"));

        assertTrue(exception.getMessage().contains("accountId"));
    }

    private JsonNode json(String value) throws Exception
    {
        return objectMapper.readTree(value);
    }
}
