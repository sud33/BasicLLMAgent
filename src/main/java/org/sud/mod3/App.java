package org.sud.mod3;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.sud.mod3.agent.PaymentInvestigationAgent;
import org.sud.mod3.repository.AccountRepository;
import org.sud.mod3.repository.CustomerRepository;
import org.sud.mod3.repository.LedgerRepository;
import org.sud.mod3.repository.PaymentRepository;
import org.sud.mod3.tools.PaymentInvestigationTools;

import java.util.Scanner;

/**
 * Console entry point for the Payment Investigation Agent example.
 *
 * <p>The application assembles in-memory repositories, exposes them as tools,
 * and starts an LLM-controlled agent loop. Set {@code OPENAI_API_KEY} before
 * running the application.</p>
 */
public class App
{
    /**
     * Reads one investigation goal and displays each LLM and tool interaction.
     *
     * @param args command-line arguments; they are not used
     */
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter investigation request:");
        System.out.print("> ");
        String request = scanner.nextLine().trim();

        if (request.isEmpty()) {
            System.out.println("Please enter a payment investigation request.");
            return;
        }

        PaymentInvestigationTools tools = new PaymentInvestigationTools(
                new PaymentRepository(),
                new AccountRepository(),
                new CustomerRepository(),
                new LedgerRepository());

        OpenAIClient client = OpenAIOkHttpClient.fromEnv();
        PaymentInvestigationAgent agent =
                new PaymentInvestigationAgent(client, tools, System.out);

        System.out.println();
        System.out.println("Agent investigation started...");
        System.out.println();
        agent.investigate(request);
    }
}
