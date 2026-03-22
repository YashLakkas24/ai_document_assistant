package org.example.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocService {

    private final ChatClient chatClient;
    private final DocumentParserService extractionService;
    private final DocumentProcessorService processorService;
    private final DocumentChunkService chunkService;
    private final EmbeddingModel embeddingModel;
    private final SimilarityService similarityService;

    @Value("classpath:prompts/system-prompt.st")
    private Resource systemPrompt;

    @Value("classpath:prompts/document-query.st")
    private Resource userPrompt;


    public DocService(ChatClient chatClient, DocumentParserService extractionService, DocumentProcessorService processorService, DocumentChunkService chunkService, EmbeddingModel embeddingModel, SimilarityService similarityService) {
        this.chatClient = chatClient;
        this.extractionService = extractionService;
        this.processorService = processorService;
        this.chunkService = chunkService;
        this.embeddingModel = embeddingModel;
        this.similarityService = similarityService;
    }

    public Flux<String> askChunk(String chunk, String question) {
        PromptTemplate template = new PromptTemplate(userPrompt);
        String userText = template.create(Map.of("document", chunk, "question", question)).getContents();

        return chatClient.prompt().system(systemPrompt).user(userText).stream().content();
    }

    public Flux<String> askAI(MultipartFile file, String question) {
        String text = extractionService.extractText(file);
        text = processorService.cleanText(text);
        List<String> chunks = chunkService.splitIntoChunks(text, 1000);
        return Flux.fromIterable(chunks).flatMap(chunk -> askChunk(chunk, question));
    }

    public List<float[]> createEmbeddings(List<String> chunks) {
        return embeddingModel.embed(chunks);
    }

    public List<float[]> embedQuery(List<String> question) {
        return embeddingModel.embed(question);
    }

    public List<String> getRelevantChunks(List<String> chunks, String question) {
        float[] questionEmbedding = embeddingModel.embed(question);
        Map<String, Double> scoreMap = new HashMap<>();
        List<float[]> chunkEmbeddings = embeddingModel.embed(chunks);
        for (int i = 0; i < chunks.size(); i++) {
            float[] chunkVector = chunkEmbeddings.get(i);
            double score = similarityService.cosineSimilarity(questionEmbedding, chunkVector);
            scoreMap.put(chunks.get(i), score);
        }
        return scoreMap.entrySet()
                .stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }
}