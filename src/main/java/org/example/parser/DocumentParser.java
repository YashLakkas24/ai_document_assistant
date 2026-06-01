package org.example.parser;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentParser {
    String extract(MultipartFile file);
}
