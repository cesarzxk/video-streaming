package com.cesarzxk.initial.videoStreaming.controller;

import com.cesarzxk.initial.videoStreaming.domain.Video;
import com.cesarzxk.initial.videoStreaming.services.VideoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/videos")
public class VideoController {
    private final VideoService videoService;

    public VideoController(VideoService videoService)
    {
        this.videoService = videoService;
    }

    @GetMapping("/")
    public List<Video> listVideos() throws Exception {
        return videoService.getAllVideos();
    }

}