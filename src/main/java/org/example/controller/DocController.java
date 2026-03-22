package org.example.controller;


import org.example.service.DocService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

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
