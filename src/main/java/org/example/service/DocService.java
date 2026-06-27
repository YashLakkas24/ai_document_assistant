package org.example.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;


import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DocService {

    private final ChatClient chatClient;
    private final DocumentParserService extractionService;
    private final DocumentProcessorService processorService;
    private final DocumentChunkService chunkService;
    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    @Value("classpath:prompts/system-prompt.st")
    private Resource systemPrompt;

    @Value("classpath:prompts/document-query.st")
    private Resource userPrompt;

    public DocService(ChatClient chatClient, DocumentParserService extractionService, DocumentProcessorService processorService, DocumentChunkService chunkService, VectorStore vectorStore, JdbcTemplate jdbcTemplate) {
        this.chatClient = chatClient;
        this.extractionService = extractionService;
        this.processorService = processorService;
        this.chunkService = chunkService;
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Flux<String> askChunk(String chunk, String question) {
        PromptTemplate template = new PromptTemplate(userPrompt);
        String userText = template.create(Map.of("document", chunk, "question", question)).getContents();

        return chatClient.prompt().system(systemPrompt).user(userText).stream().content();
    }

    public Flux<String> askQuestion(String question, String userId) {

        List<String> relevantChunks = getRelevantChunks(question);
        String combinedContext = String.join("\n\n", relevantChunks);

        PromptTemplate template = new PromptTemplate(userPrompt);
        String userText = template.create(Map.of("document", combinedContext, "question", question)).getContents();
        return chatClient
                .prompt()
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, userId))
                .system(systemPrompt)
                .user(userText)
                .stream()
                .content();
    }

    public List<String> getRelevantChunks(String question) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query(question).topK(5).build());
        return results.stream().map(Document::getFormattedContent).toList();
    }

    public void processAndStore(MultipartFile file, String userId) {

        String text = extractionService.extractText(file);
        text = processorService.cleanText(text);
        List<String> chunks = chunkService.splitIntoChunks(text, 1000);
        List<Document> documents = chunks.stream().map(chunk -> Document.builder().text(chunk).metadata("filename", file.getOriginalFilename()).build()).toList();
        vectorStore.add(documents);
    }
}