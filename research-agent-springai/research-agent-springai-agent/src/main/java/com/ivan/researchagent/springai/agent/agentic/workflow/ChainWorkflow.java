package com.ivan.researchagent.springai.agent.agentic.workflow;

import org.springframework.ai.chat.client.ChatClient;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * Implements the Prompt Chaining workflow pattern for decomposing complex tasks
 *  * into a sequence
 *  * of LLM calls where each step processes the output of the previous one.
 *  *
 *  * <p>
 *  * This implementation demonstrates a four-step workflow for processing
 *  * numerical data in text:
 *  * <ol>
 *  * <li>Extract numerical values and metrics</li>
 *  * <li>Standardize to percentage format</li>
 *  * <li>Sort in descending order</li>
 *  * <li>Format as markdown table</li>
 *  * </ol>
 *  *
 *  * <p/>
 *  * When to use this workflow: This workflow is ideal for situations where the
 *  * task can be easily and cleanly decomposed into fixed subtasks. The main goal
 *  * is to trade off latency for higher accuracy, by making each LLM call an
 *  * easier task.
 *
 * @version 1.0
 * @description: 链式工作流
 * @author: ivan
 * @since: 2025/3/31/周一
 **/
public class ChainWorkflow {

    /**
     * Array of system prompts that define the transformation steps in the chain.
     * Each prompt acts as a gate that validates and transforms the output before
     * proceeding to the next step.
     */
    private static final String[] DEFAULT_SYSTEM_PROMPTS = {

            // Step 1
            """
					Extract only the numerical values and their associated metrics from the text.
					Format each as'value: metric' on a new line.
					Example format:
					92: customer satisfaction
					45%: revenue growth""",
            // Step 2
            """
					Convert all numerical values to percentages where possible.
					If not a percentage or points, convert to decimal (e.g., 92 points -> 92%).
					Keep one number per line.
					Example format:
					92%: customer satisfaction
					45%: revenue growth""",
            // Step 3
            """
					Sort all lines in descending order by numerical value.
					Keep the format 'value: metric' on each line.
					Example:
					92%: customer satisfaction
					87%: employee satisfaction""",
            // Step 4
            """
					Format the sorted data as a markdown table with columns:
					| Metric | Value |
					|:--|--:|
					| Customer Satisfaction | 92% | """
    };

    private final ChatClient chatClient;

    private final String[] systemPrompts;

    /**
     * Constructs a new instance of the Prompt Chaining workflow with the specified
     * chat client and default system prompts.
     *
     * @param chatClient the Spring AI chat client used to make LLM calls
     */
    public ChainWorkflow(ChatClient chatClient) {
        this(chatClient, DEFAULT_SYSTEM_PROMPTS);
    }

    /**
     * Constructs a new instance of the Prompt Chaining workflow with the specified
     * chat client and system prompts.
     *
     * @param chatClient    the Spring AI chat client used to make LLM calls
     * @param systemPrompts the system prompts that define the transformation steps
     *                      in the chain
     */
    public ChainWorkflow(ChatClient chatClient, String[] systemPrompts) {
        this.chatClient = chatClient;
        this.systemPrompts = systemPrompts;
    }

    /**
     * Executes the prompt chaining workflow by processing the input text through
     * a series of LLM calls, where each call's output becomes the input for the
     * next step.
     *
     * <p>
     * The method prints the intermediate results after each step to show the
     * progression of transformations through the chain.
     *
     * @param chatClient the Spring AI chat client used to make LLM calls
     * @param userInput     the input text containing numerical data to be processed
     * @return the final output after all steps have been executed
     */
    public String chain(String userInput) {

        int step = 0;
        String response = userInput;
        System.out.println(String.format("\nSTEP %s:\n %s", step++, response));

        for (String prompt : systemPrompts) {

            // 1. Compose the input using the response from the previous step.
            String input = String.format("{%s}\n {%s}", prompt, response);

            // 2. Call the chat client with the new input and get the new response.
            response = chatClient.prompt(input).call().content();

            System.out.println(String.format("\nSTEP %s:\n %s", step++, response));
        }

        return response;
    }
}
