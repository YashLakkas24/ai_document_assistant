package org.example.controller;

import org.example.exception.InvalidRequestException;
import org.example.service.DocService;
import org.example.service.JwtService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/doc")
public class DocController {
    private final DocService docService;
    private final JwtService jwtService;

    public DocController(DocService docService, JwtService jwtService) {
        this.docService = docService;
        this.jwtService = jwtService;
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String authHeader) {
        if (file.isEmpty()) {
            throw new InvalidRequestException("Send valid file.");
        }
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        docService.processAndStore(file, username);
        return "Document uploaded and processed successfully.";
    }

    @PostMapping(value = "/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam("question") String question, @RequestHeader("Authorization") String authHeader) {
        if (question.isBlank()) {
            throw new InvalidRequestException("Question cannot be empty.");
        }
        String token = authHeader.substring(7);
        String userId = jwtService.extractUsername(token);

        if (userId.isBlank()) {
            throw new InvalidRequestException("UserId cannot be empty.");
        }

        return docService.askQuestion(question, userId);
    }
}
