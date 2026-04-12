package com.cesarzxk.initial.videoStreaming.repositories;

import com.cesarzxk.initial.videoStreaming.domain.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {
}