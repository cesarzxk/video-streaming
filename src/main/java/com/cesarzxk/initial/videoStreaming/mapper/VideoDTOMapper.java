package com.cesarzxk.initial.videoStreaming.mapper;

import com.cesarzxk.initial.videoStreaming.domain.Video;
import com.cesarzxk.initial.videoStreaming.dto.VideoRequestDTO;
import com.cesarzxk.initial.videoStreaming.dto.VideoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class VideoDTOMapper {
    public VideoRequestDTO toVideoRequestDTO(Video video) {
        return VideoRequestDTO.builder()
                .title(video.getTitle())
                .url(video.getUrl())
                .duration(video.getDuration())
                .thumbnailUrl(video.getThumbnailUrl())
                .description(video.getDescription())
                .uploaderName(video.getUploaderName())
                .qualities(video.getQuality())
                .build();
    }

    public Video toVideoEntity(VideoRequestDTO video) {
        return Video.builder()
                .title(video.getTitle())
                .url(video.getUrl())
                .duration(video.getDuration())
                .thumbnailUrl(video.getThumbnailUrl())
                .description(video.getDescription())
                .uploaderName(video.getUploaderName())
                .quality(video.getQualities())
                .build();
    }

    public VideoResponseDTO toVideoResponseDTO(Video video) {
        return VideoResponseDTO.builder()
                .id(video.getId())
                .title(video.getTitle())
                .url(video.getUrl())
                .duration(video.getDuration())
                .thumbnailUrl(video.getThumbnailUrl())
                .description(video.getDescription())
                .uploaderName(video.getUploaderName())
                .uploadDate(video.getUploadDate())
                .quality(video.getQuality())
                .build();
    }
}
