package org.sud.mod1;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;

import java.math.BigDecimal;
import java.util.Scanner;

/**
 * Demonstrates a basic Java application that sends a simple addition request to
 * the OpenAI Chat Completions API.
 *
 * <p>The application reads two numbers from standard input, asks the model to add
 * them, validates that the response is numeric, and formats the result to match
 * the inputs. If both inputs are whole numbers, the result has no decimal point.
 * If either input contains a decimal point, the result retains an appropriate
 * number of decimal places.</p>
 *
 * <p>The OpenAI Java client reads its configuration from the environment. Set
 * {@code OPENAI_API_KEY} before running the application.</p>
 */
public class App
{
    /**
     * Runs the interactive addition example.
     *
     * @param args command-line arguments; they are not used
     */
    public static void main( String[] args )
    {
        Scanner scanner = new Scanner(System.in);

        // Keep the original text so decimal points and trailing zeros can influence
        // the final display format (for example, "2.0" produces a decimal result).
        System.out.print("Enter the first number: ");
        String firstInput = scanner.nextLine().trim();

        System.out.print("Enter the second number: ");
        String secondInput = scanner.nextLine().trim();

        BigDecimal firstNumber;
        BigDecimal secondNumber;
        try {
            firstNumber = new BigDecimal(firstInput);
            secondNumber = new BigDecimal(secondInput);
        } catch (NumberFormatException exception) {
            System.out.println("Please enter valid numbers.");
            return;
        }

        // A decimal point in either original input means the result should also
        // be displayed with a decimal point.
        boolean hasDecimalInput = firstInput.contains(".") || secondInput.contains(".");

        // fromEnv() obtains the API key and other supported settings from the environment.
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        ChatCompletionCreateParams.Builder params = ChatCompletionCreateParams.builder().model(ChatModel.GPT_4).maxCompletionTokens(1024);

        // The system message defines both the task and the response format so the
        // returned content can be parsed as a number without removing extra prose.
        ChatCompletionSystemMessageParam sysMsg = ChatCompletionSystemMessageParam.builder()
                .content("Add the two input numbers and return only the numeric result. "
                        + "Return a whole number without a decimal point when both inputs are whole numbers. "
                        + "Return a number with a decimal point when either input contains a decimal point.")
                .build();

        // The user message supplies the specific operands for this request.
        ChatCompletionUserMessageParam userMsg = ChatCompletionUserMessageParam.builder()
                .content("Add " + firstInput + " and " + secondInput
                        + ". Decimal output required: " + hasDecimalInput)
                .build();

        params.addMessage(sysMsg);
        params.addMessage(userMsg);

        ChatCompletion completion = client.chat().completions().create(params.build());

        // Treat a non-numeric model response as an error instead of displaying it
        // as though it were a valid sum.
        String aiResult = completion.choices().get(0).message().content().orElse("").trim();
        BigDecimal numericResult;
        try {
            numericResult = new BigDecimal(aiResult);
        } catch (NumberFormatException exception) {
            System.out.println("The AI returned an invalid numeric result: " + aiResult);
            return;
        }

        // Match the output precision to the inputs. Whole-number inputs produce
        // an integer; decimal inputs preserve at least the greatest input scale.
        String formattedResult;
        if (hasDecimalInput) {
            int decimalPlaces = Math.max(1, Math.max(firstNumber.scale(), secondNumber.scale()));
            formattedResult = numericResult.setScale(decimalPlaces).toPlainString();
        } else {
            formattedResult = numericResult.toBigIntegerExact().toString();
        }

        System.out.println("AI result: " + formattedResult);


    }
}
