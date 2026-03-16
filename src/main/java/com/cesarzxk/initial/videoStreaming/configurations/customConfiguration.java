package com.cesarzxk.initial.videoStreaming.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class customConfiguration {
    @Bean
    public String SdkMock (){
        return "SDKMOCK";

    }
}
