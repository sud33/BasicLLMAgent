# Basic LLM Agent

A small Java project containing two introductory examples of calling an OpenAI
model with the OpenAI Java SDK. Both programs are interactive console
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
├── mod1/App.java   # Addition agent
└── mod2/App.java   # General basic-math agent
```
