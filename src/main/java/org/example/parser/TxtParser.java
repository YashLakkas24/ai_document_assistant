package org.example.parser;

import org.springframework.web.multipart.MultipartFile;

public class TxtParser implements DocumentParser {
    @Override
    public String extract(MultipartFile file) {
        try {
            return new String(file.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Error reading TXT ", e);
        }
    }
}
