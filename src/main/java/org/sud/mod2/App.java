package org.sud.mod2;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;

import java.util.Scanner;

/**
 * Demonstrates a basic Java application that uses the OpenAI Chat Completions
 * API as a general-purpose calculator.
 *
 * <p>The application reads a math expression or question from standard input
 * and asks the model to solve it. It supports common arithmetic operations such
 * as addition, subtraction, multiplication, division, remainders, powers,
 * roots, percentages, and expressions containing parentheses.</p>
 *
 * <p>The OpenAI Java client reads its configuration from the environment. Set
 * {@code OPENAI_API_KEY} before running the application.</p>
 */
public class App
{
    /**
     * Runs the interactive basic-math example.
     *
     * @param args command-line arguments; they are not used
     */
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a basic math expression or question: ");
        String mathRequest = scanner.nextLine().trim();

        // Avoid making an API request when the user has not supplied a problem.
        if (mathRequest.isEmpty()) {
            System.out.println("Please enter a math expression or question.");
            return;
        }

        // fromEnv() obtains the API key and other supported settings from the environment.
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        ChatCompletionCreateParams.Builder params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4)
                .maxCompletionTokens(1024);

        // The system message defines the calculator's supported scope and keeps
        // the response concise enough to display directly in the console.
        ChatCompletionSystemMessageParam systemMessage = ChatCompletionSystemMessageParam.builder()
                .content("You are a calculator for basic mathematics. "
                        + "Solve addition, subtraction, multiplication, division, remainders, "
                        + "powers, roots, percentages, and expressions with parentheses. "
                        + "Follow the standard order of operations. "
                        + "Return only the final answer without explanatory text. "
                        + "If the expression is invalid or undefined, return a short error message. "
                        + "Do not answer requests unrelated to basic mathematics.")
                .build();

        // The user message contains the exact expression or math question entered
        // for this invocation.
        ChatCompletionUserMessageParam userMessage = ChatCompletionUserMessageParam.builder()
                .content("Solve this basic math problem: " + mathRequest)
                .build();

        params.addMessage(systemMessage);
        params.addMessage(userMessage);

        ChatCompletion completion = client.chat().completions().create(params.build());

        // An empty response is handled explicitly instead of printing a blank result.
        String result = completion.choices().get(0).message().content().orElse("").trim();
        if (result.isEmpty()) {
            System.out.println("The AI did not return a result.");
            return;
        }

        System.out.println("AI result: " + result);
    }
}
