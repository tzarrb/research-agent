package com.ivan.researchagent.springai.llm.configuration.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/24/周二
 **/
@Configuration
public class RagPromptTemplateConfiguration {

    @Bean
    public PromptTemplate transformerPromptTemplate() {

        return new PromptTemplate(
                """
                Given a user query, rewrite the user question to provide better results when querying {target}.
                                
                You should follow these rules:
                                
                1. Remove any irrelevant information and make sure the query is concise and specific;
                2. The output must be consistent with the language of the user's query;
                3. Ensure better understanding and answers from the perspective of large models.
                
                Original query:
                {query}
                
                Query after rewrite:
                """
        );
    }


    @Bean
    public PromptTemplate queryArgumentPromptTemplate() {

        return new PromptTemplate(
                """
                You'll get a set of document contexts that are relevant to the issue.
                Each document begins with a reference number, such as [[x]], where x is a number that can be repeated.
                Documents that are not referenced will be marked as [[null]].
                Use context and refer to it at the end of each sentence, if applicable.
                The context information is as follows:
                
                ---------------------
                {context}
                ---------------------
                
                Generate structured responses to user questions given contextual information and without prior knowledge.
                                
                When you answer user questions, follow these rules:
                
                1. If the answer is not in context, say you don't know;
                2. Don't provide any information that is not relevant to the question, and don't output any duplicate content;
                3. Avoid using "context-based..." or "The provided information..." said;
                4. Your answers must be correct, accurate, and written in an expertly unbiased and professional tone;
                5. The appropriate text structure in the answer is determined according to the characteristics of the content, please include subheadings in the output to improve readability;
                6. When generating a response, provide a clear conclusion or main idea first, without a title;
                7. Make sure each section has a clear subtitle so that users can better understand and refer to your output;
                8. If the information is complex or contains multiple sections, make sure each section has an appropriate heading to create a hierarchical structure;
                9. Please refer to the sentence or section with the reference number at the end in [[x]] format;
                10. If a sentence or section comes from more than one context, list all applicable references, e.g. [[x]][[y]];
                11. Your output answers must be in beautiful and rigorous markdown format.
                12. Because your output is in markdown format, please include the link in the reference document in the form of a hyperlink when referencing the context, so that users can click to view it;
                13. If a reference is marked as [[null]], it does not have to be cited;
                14. Except for Code. Aside from the specific name and citation, your answer must be written in the same language as the question.
                
                User Issue:
                
                {query}
                                
                Your answer:
                """
        );
    }


    @Bean
    public PromptTemplate qaPromptTemplate() {

        return new PromptTemplate(
                """
                Context information is below.
                ---------------------
                {question_answer_context}
                ---------------------
                Given the context and provided history information and not prior knowledge,
                reply to the user comment. If the answer is not in the context, inform
                the user that you can't answer the question.
                """
        );
    }

}
