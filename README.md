# Basic LLM Agent

A small Java project containing three examples of calling an OpenAI model with
the OpenAI Java SDK. All programs are interactive console
applications and have their own `main` method.

## Examples

### 1. Addition agent

Main class: `org.sud.mod1.App`

This example asks the user for two numbers and sends them to the model for
addition. It demonstrates:

- reading and validating console input;
- using a **system message** to define the model's role and output rules;
- using a **user message** to provide the values for a specific request;
- calling the Chat Completions API with the OpenAI Java SDK;
- validating the model's response; and
- formatting the result as a whole number or decimal based on the input.

Example:

```text
Enter the first number: 12
Enter the second number: 8
AI result: 20
```

### 2. Basic-math agent

Main class: `org.sud.mod2.App`

This example accepts a complete math expression or question. Its system prompt
allows common arithmetic operations, including addition, subtraction,
multiplication, division, remainders, powers, roots, percentages, parentheses,
and standard order of operations.

The system message defines the calculator's capabilities and restrictions. The
user message contains the actual problem entered at runtime. Keeping those two
roles separate makes the model's general instructions reusable across many
different questions.

Example:

```text
Enter a basic math expression or question: (12 + 8) * 3
AI result: 60
```

## Payment Investigation Agent

Main class: `org.sud.mod3.App`

### What does this demonstrate?

This example demonstrates a genuine LLM-controlled agent loop. Unlike a
traditional workflow, the application does not determine the investigation
sequence in advance. Java exposes payment, account, customer, and ledger
capabilities as tools. The LLM examines the user's goal, selects a tool,
evaluates its result, and then decides whether another tool is required. This
continues until the model has enough evidence to answer the original question.

> **Java defines what the agent CAN do. The LLM decides what the agent SHOULD do
> next.**

The Java code contains no `if payment failed, then get account` investigation
logic. Different questions and tool observations can therefore produce
different paths through the same four capabilities.

### Traditional workflow vs agent

In a traditional workflow, the developer fixes the sequence in application
code:

```text
getPayment()
     |
     v
if failed
     |
     v
getAccount()
     |
     v
getLedger()
```

In the agent approach, the model controls the sequence:

```text
               +----------------+
               |      LLM       |
               +-------+--------+
                       |
                 chooses tool
                       |
                       v
               +----------------+
               |   Java Tools   |
               +-------+--------+
                       |
                  observation
                       |
                       +----------> LLM
                                      |
                               enough information?
                                /             \
                              no               yes
                              |                 |
                        another tool       final answer
```

The developer defines the available capabilities. The LLM determines which
capabilities are needed and in what order.

### Available tools

- `getPayment` retrieves payment status, related IDs, amount, currency, and any
  failure code.
- `getAccount` retrieves an account's type, owner, status, and closed date.
- `getCustomer` retrieves a customer's lifecycle status and risk level.
- `getLedgerEntries` retrieves debit, credit, and reversal activity and its
  posting status for a payment.

The tool descriptions explain the information each capability provides. They
do not prescribe an investigation workflow.

### Example agent flow

For this request:

```text
Investigate PAY-1004 and tell me why it failed.
```

The model can discover the following path:

```text
User
 |
 v
LLM
 |
 +--> getPayment("PAY-1004")
 |         |
 |         +--> FAILED / ACCOUNT_VALIDATION_FAILED / ACC-500
 |
 +--> getAccount("ACC-500")
           |
           +--> CLOSED / 2026-07-31

LLM
 |
 v
Final explanation: the funding account was closed before the payment.
```

That path is an example, not Java application logic. The model chooses the
actual tools at runtime. Console logging displays every iteration, selected
tool, arguments, observation, and final response so the loop is easy to follow.

### Run the payment investigation agent

Set `OPENAI_API_KEY` as described below, compile the project, and run:

```bash
mvn clean compile
mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
  -Dexec.mainClass=org.sud.mod3.App
```

Example input:

```text
Investigate PAY-1004 and tell me why it failed.
```

The exact model wording and selected calls can vary, but the output will show
the agent iterations and any tools chosen by the model before its conclusion.
The loop stops with a clear error if it exceeds ten model iterations.

### Sample investigation scenarios

- `PAY-1004` — failed payment with a closed funding account
- `PAY-2001` — successful payment whose merchant credit is still pending
- `PAY-3005` — successful payment with posted debit and credit entries
- `PAY-4002` — failed validation for a suspended customer
- `PAY-9999` — nonexistent payment; the agent should not invent data

## Prerequisites

- Java 17 or later
- Apache Maven
- An OpenAI Platform account with API access and available billing or credits
- An OpenAI API key

Check the installed Java and Maven versions:

```bash
java -version
mvn -version
```

## Create an OpenAI API key

1. Sign in to the [OpenAI Platform](https://platform.openai.com/).
2. Open the [API keys page](https://platform.openai.com/api-keys).
3. Select **Create new secret key** and choose the appropriate project and
   permissions.
4. Copy the key when it is displayed. Store it securely because the complete
   secret may not be shown again.
5. If necessary, configure API billing in the OpenAI Platform. ChatGPT
   subscriptions and API usage are billed separately.

Never put an API key in Java source code, `README.md`, Git, screenshots, or
messages. If a key is accidentally exposed, revoke it in the API keys page and
create a replacement.

For more information, see the
[OpenAI API quickstart](https://developers.openai.com/api/docs/quickstart).

## Configure the API key

The programs use `OpenAIOkHttpClient.fromEnv()`, which reads the key from the
`OPENAI_API_KEY` environment variable.

### macOS or Linux

Set the variable for the current terminal session:

```bash
export OPENAI_API_KEY="your-api-key"
```

### Windows PowerShell

Set the variable for the current PowerShell session:

```powershell
$env:OPENAI_API_KEY = "your-api-key"
```

Replace `your-api-key` with the secret key you created. Do not commit the real
value to this repository.

## Build the project

From the repository root, run:

```bash
mvn clean compile
```

## Run the examples

Run the addition agent:

```bash
mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
  -Dexec.mainClass=org.sud.mod1.App
```

Run the basic-math agent:

```bash
mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
  -Dexec.mainClass=org.sud.mod2.App
```

Run the payment investigation agent:

```bash
mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
  -Dexec.mainClass=org.sud.mod3.App
```

On Windows PowerShell, enter each command on one line or replace the `\` line
continuation with a PowerShell backtick.

## How the LLM call works

Each example follows the same basic flow:

1. Read a problem from the user.
2. Create an OpenAI client from the environment.
3. Define a system message describing the model's job and constraints.
4. Define a user message containing the current problem.
5. Send both messages to the model through the Chat Completions API.
6. Read and display the first returned answer.

LLM output is generated rather than calculated by a deterministic local math
library. It can occasionally be incorrect, so these examples are intended for
learning the SDK and prompt roles, not for financial, scientific, or other
high-stakes calculations.

## Project structure

```text
src/main/java/org/sud/
├── mod1/App.java            # Addition agent
├── mod2/App.java            # General basic-math agent
└── mod3/
    ├── App.java             # Payment investigation console runner
    ├── agent/               # LLM-to-tool interaction loop
    ├── model/               # Payment domain records
    ├── repository/          # Seeded in-memory data
    └── tools/               # Tool definitions and dispatcher
```
