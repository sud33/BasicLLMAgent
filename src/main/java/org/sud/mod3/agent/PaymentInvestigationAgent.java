package org.sud.mod3.agent;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import org.sud.mod3.tools.PaymentInvestigationTools;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the LLM-controlled payment investigation loop.
 *
 * <p>Java sends the conversation and available tools to the model, executes any
 * requested tool calls, and returns the results to the model. Java does not
 * encode an investigation sequence. After every observation, the model decides
 * whether it needs another tool or is ready to answer the user.</p>
 */
public class PaymentInvestigationAgent
{
    static final int MAX_ITERATIONS = 10;

    private static final String SYSTEM_INSTRUCTIONS = """
            You are a payment investigation agent.

            Investigate payment-related questions using the available tools. Do not make
            assumptions about payment, account, customer, or ledger data. Use tools whenever
            information is required, and do not call tools unnecessarily.

            You may call multiple tools during one investigation. After each tool result,
            decide whether you have enough evidence to answer the user's question or whether
            another tool is necessary. If a record cannot be found, say so and do not invent it.

            When the investigation is complete, explain the conclusion in clear language and
            mention the relevant evidence. If the available data is insufficient to determine
            the cause, say that explicitly instead of guessing.
            """;

    private final OpenAIClient client;
    private final PaymentInvestigationTools tools;
    private final PrintStream output;

    public PaymentInvestigationAgent(
            OpenAIClient client,
            PaymentInvestigationTools tools,
            PrintStream output)
    {
        this.client = client;
        this.tools = tools;
        this.output = output;
    }

    /**
     * Investigates one user goal until the model answers or the safety limit is reached.
     *
     * @param userGoal the payment question to investigate
     * @return the model's final human-readable conclusion
     */
    public String investigate(String userGoal)
    {
        if (userGoal == null || userGoal.isBlank()) {
            throw new IllegalArgumentException("An investigation request is required");
        }

        List<ChatCompletionMessageParam> conversation = new ArrayList<>();
        conversation.add(ChatCompletionMessageParam.ofSystem(
                ChatCompletionSystemMessageParam.builder().content(SYSTEM_INSTRUCTIONS).build()));
        conversation.add(ChatCompletionMessageParam.ofUser(
                ChatCompletionUserMessageParam.builder().content(userGoal.trim()).build()));

        for (int iteration = 1; iteration <= MAX_ITERATIONS; iteration++) {
            logIteration(iteration);

            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_4)
                    .maxCompletionTokens(1024)
                    .messages(conversation)
                    .tools(tools.definitions())
                    .build();

            ChatCompletion completion = client.chat().completions().create(request);
            ChatCompletionMessage assistantMessage = completion.choices().get(0).message();
            List<ChatCompletionMessageToolCall> toolCalls =
                    assistantMessage.toolCalls().orElse(List.of());

            // Preserve the assistant's tool-call message before adding observations.
            // This gives the model the complete context needed to choose its next action.
            conversation.add(ChatCompletionMessageParam.ofAssistant(assistantMessage.toParam()));

            if (toolCalls.isEmpty()) {
                String finalAnswer = assistantMessage.content()
                        .filter(content -> !content.isBlank())
                        .orElseThrow(() -> new IllegalStateException(
                                "The model returned neither a tool call nor a final answer"));
                output.println("Final response:");
                output.println(finalAnswer);
                return finalAnswer;
            }

            for (ChatCompletionMessageToolCall toolCall : toolCalls) {
                executeToolCall(toolCall, conversation);
            }
        }

        throw new IllegalStateException(
                "Payment investigation exceeded the maximum of " + MAX_ITERATIONS + " iterations");
    }

    private void executeToolCall(
            ChatCompletionMessageToolCall toolCall,
            List<ChatCompletionMessageParam> conversation)
    {
        String toolName = toolCall.function().name();
        String arguments = toolCall.function().arguments();

        output.println("LLM requested tool: " + toolName);
        output.println("Arguments:");
        output.println(arguments);

        String result;
        try {
            result = tools.execute(toolName, arguments);
        } catch (RuntimeException exception) {
            result = "{\"error\":\"Tool execution failed: "
                    + escapeJson(exception.getMessage()) + "\"}";
        }

        output.println("Tool result:");
        output.println(result);

        // Tool results are observations, not final answers. They are added to the
        // conversation so the model can decide what information it needs next.
        ChatCompletionToolMessageParam toolResult = ChatCompletionToolMessageParam.builder()
                .toolCallId(toolCall.id())
                .content(result)
                .build();
        conversation.add(ChatCompletionMessageParam.ofTool(toolResult));
    }

    private void logIteration(int iteration)
    {
        output.println("--------------------------------------------------");
        output.println("Agent Iteration " + iteration);
        output.println("--------------------------------------------------");
    }

    private String escapeJson(String value)
    {
        if (value == null) {
            return "Unknown error";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
