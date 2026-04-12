package com.cesarzxk.initial.videoStreaming.dto;

import com.cesarzxk.initial.videoStreaming.domain.Quality;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoResponseDTO {
    private Long id;
    private String title;
    private String url;
    private Long duration;
    private String thumbnailUrl;
    private String description;
    private String uploaderName;
    private List<Quality> quality;
    private LocalDateTime uploadDate;
}