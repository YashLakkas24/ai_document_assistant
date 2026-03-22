package org.example.service;

import org.springframework.stereotype.Service;

@Service
public class DocumentProcessorService {
    public String cleanText(String text) {
        if (text == null) {
            return null;
        }
        text = text.replaceAll("\\s+", " ");
        text = text.replaceAll("Page \\d+", "");
        text = text.replaceAll("[^\\x00-\\x7F]", "");
        return text.trim();
    }
}
