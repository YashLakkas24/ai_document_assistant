package org.example.service;

import org.example.exception.InvalidFileTypeException;
import org.example.parser.DocumentParser;
import org.example.parser.DocxParser;
import org.example.parser.PdfParser;
import org.example.parser.TxtParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentParserService {
    public String extractText(MultipartFile file) {
        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new InvalidFileTypeException("File name is null");
        }

        DocumentParser parser;

        if (fileName.endsWith(".pdf")) {
            parser = new PdfParser();
        } else if (fileName.endsWith(".doc")) {
            parser = new DocxParser();
        } else if (fileName.endsWith(".txt")) {
            parser = new TxtParser();
        } else {
            throw new InvalidFileTypeException("Unsupported file type");
        }
        return parser.extract(file);
    }
}
