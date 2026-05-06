package com.hms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public")
public class FileUploadController {

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
            String contentType = file.getContentType();
            if (contentType == null) contentType = "image/png";
            
            String dataUri = "data:" + contentType + ";base64," + base64;

            return ResponseEntity.ok(Map.of(
                    "url", dataUri,
                    "message", "File uploaded successfully"
            ));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("message", "Could not process file: " + e.getMessage()));
        }
    }
}
