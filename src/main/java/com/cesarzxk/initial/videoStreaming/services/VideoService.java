package com.cesarzxk.initial.videoStreaming.services;

import com.cesarzxk.initial.videoStreaming.domain.Video;
import com.cesarzxk.initial.videoStreaming.dto.VideoRequestDTO;
import com.cesarzxk.initial.videoStreaming.dto.VideoResponseDTO;
import com.cesarzxk.initial.videoStreaming.mapper.VideoDTOMapper;
import com.cesarzxk.initial.videoStreaming.repositories.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoRepository videoRepository;
    final private VideoDTOMapper videoDTOMapper;

    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    public VideoResponseDTO setVideo(VideoRequestDTO video) {
        Video newVideo = videoDTOMapper.toVideoEntity(video);
        Video savedVideo = videoRepository.save(newVideo);
        return videoDTOMapper.toVideoResponseDTO(savedVideo);
    }
}