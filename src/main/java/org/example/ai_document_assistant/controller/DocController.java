package org.example.ai_document_assistant.controller;

import org.example.ai_document_assistant.service.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import javax.print.attribute.standard.Media;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doc")
public class DocController {
    private final DocService docService;

    public DocController(DocService docService) {
        this.docService = docService;
    }

    @PostMapping("/ask")
    public Flux<String> chat(@RequestParam("file") MultipartFile file, @RequestParam String question) {
        return docService.askAI(file, question);
    }
}
