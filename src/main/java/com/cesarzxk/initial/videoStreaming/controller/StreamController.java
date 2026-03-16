package com.cesarzxk.initial.videoStreaming.controller;

import com.cesarzxk.initial.videoStreaming.dto.UploadResponseDTO;
import com.cesarzxk.initial.videoStreaming.services.StreamService;
import com.cesarzxk.initial.videoStreaming.services.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/stream")
public class StreamController {
    private final StreamService streamService;
    private final StorageService storageService;

    public StreamController(StreamService streamService, StorageService storageService) {
        this.streamService = streamService;
        this.storageService = storageService;
    }

    @GetMapping("/{name:.+}")
    public ResponseEntity<Resource> streamVideo(@PathVariable String name,
                                                @RequestHeader(value = "Range", required = false) String rangeHeader) throws Exception {
        return streamService.getStreamVideo(name, rangeHeader);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Arquivo vazio"
            ));
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("video/")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Apenas arquivos de vídeo são permitidos"
            ));
        }

        try {
            UploadResponseDTO dto = streamService.convertVideos(file);
            return ResponseEntity.ok(Map.of(
                    "message", dto.getMessage(),
                    "files", dto.getFiles(),
                    "urls", dto.getUrls()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erro ao processar vídeo",
                    "details", e.getMessage()
            ));
        }
    }
}