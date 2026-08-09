package org.sud.mod3.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionTool;
import org.sud.mod3.repository.AccountRepository;
import org.sud.mod3.repository.CustomerRepository;
import org.sud.mod3.repository.LedgerRepository;
import org.sud.mod3.repository.PaymentRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Defines the Java capabilities exposed to the model and dispatches requested
 * tool calls to the corresponding in-memory repository.
 *
 * <p>This class defines what the agent can do. It intentionally contains no
 * investigation workflow; the model decides which tool to call and when.</p>
 */
public class PaymentInvestigationTools
{
    public static final String GET_PAYMENT = "getPayment";
    public static final String GET_ACCOUNT = "getAccount";
    public static final String GET_CUSTOMER = "getCustomer";
    public static final String GET_LEDGER_ENTRIES = "getLedgerEntries";

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final LedgerRepository ledgerRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<ChatCompletionTool> definitions;

    public PaymentInvestigationTools(
            PaymentRepository paymentRepository,
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            LedgerRepository ledgerRepository)
    {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.ledgerRepository = ledgerRepository;
        this.definitions = createDefinitions();
    }

    public List<ChatCompletionTool> definitions()
    {
        return definitions;
    }

    /** Executes exactly the tool selected by the model. */
    public String execute(String toolName, String jsonArguments)
    {
        return switch (toolName) {
            case GET_PAYMENT -> findOne(
                    "payment", paymentRepository.findById(requiredArgument(jsonArguments, "paymentId")));
            case GET_ACCOUNT -> findOne(
                    "account", accountRepository.findById(requiredArgument(jsonArguments, "accountId")));
            case GET_CUSTOMER -> findOne(
                    "customer", customerRepository.findById(requiredArgument(jsonArguments, "customerId")));
            case GET_LEDGER_ENTRIES -> {
                String paymentId = requiredArgument(jsonArguments, "paymentId");
                yield toJson(Map.of(
                        "paymentId", paymentId,
                        "entries", ledgerRepository.findByPaymentId(paymentId)));
            }
            default -> toJson(Map.of(
                    "error", "Unknown tool",
                    "toolName", toolName));
        };
    }

    private List<ChatCompletionTool> createDefinitions()
    {
        return List.of(
                tool(GET_PAYMENT,
                        "Retrieve the current status and basic information for a payment, "
                                + "including related customer and account IDs, amount, currency, "
                                + "and any failure code.",
                        "paymentId", "The payment identifier, for example PAY-1004"),
                tool(GET_ACCOUNT,
                        "Retrieve an account's type, status, owner, and closed date when present.",
                        "accountId", "The customer or merchant account identifier"),
                tool(GET_CUSTOMER,
                        "Retrieve a customer's name, lifecycle status, and risk level.",
                        "customerId", "The customer identifier"),
                tool(GET_LEDGER_ENTRIES,
                        "Retrieve debit, credit, and reversal ledger activity for a payment, "
                                + "including each entry's posting status.",
                        "paymentId", "The payment identifier whose ledger activity is needed")
        );
    }

    private ChatCompletionTool tool(
            String name,
            String description,
            String argumentName,
            String argumentDescription)
    {
        Map<String, Object> argumentSchema = Map.of(
                "type", "string",
                "description", argumentDescription);

        FunctionParameters parameters = FunctionParameters.builder()
                .putAdditionalProperty("type", JsonValue.from("object"))
                .putAdditionalProperty("properties", JsonValue.from(Map.of(argumentName, argumentSchema)))
                .putAdditionalProperty("required", JsonValue.from(List.of(argumentName)))
                .putAdditionalProperty("additionalProperties", JsonValue.from(false))
                .build();

        FunctionDefinition function = FunctionDefinition.builder()
                .name(name)
                .description(description)
                .parameters(parameters)
                .strict(true)
                .build();

        return ChatCompletionTool.builder()
                .function(function)
                .build();
    }

    private String requiredArgument(String jsonArguments, String argumentName)
    {
        try {
            JsonNode arguments = objectMapper.readTree(jsonArguments);
            JsonNode value = arguments.get(argumentName);
            if (value == null || !value.isTextual() || value.asText().isBlank()) {
                throw new IllegalArgumentException("Missing required argument: " + argumentName);
            }
            return value.asText();
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Tool arguments are not valid JSON", exception);
        }
    }

    private String findOne(String resultName, Optional<?> result)
    {
        if (result.isEmpty()) {
            return toJson(Map.of(
                    "found", false,
                    "message", "No " + resultName + " was found for the supplied identifier"));
        }
        return toJson(Map.of(
                "found", true,
                resultName, result.get()));
    }

    private String toJson(Object value)
    {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize tool result", exception);
        }
    }
}
