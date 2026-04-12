package com.cesarzxk.initial.videoStreaming.domain;

import lombok.Getter;

@Getter
public enum Quality {
    Q144("144p"),
    Q240("240p"),
    Q360("360p"),
    Q480("480p"),
    Q720("720p"),
    Q1080("1080p"),
    Q1440("1440p");

    private final String label;

    Quality(String label) {
        this.label = label;
    }
}