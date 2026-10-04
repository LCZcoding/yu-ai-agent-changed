package com.lcz.imagessearchmcpserver;

import com.lcz.imagessearchmcpserver.tools.UnsplashImageTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ImagesSearchMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImagesSearchMcpServerApplication.class, args);
    }
    @Bean
    public ToolCallbackProvider imageSearchTools(UnsplashImageTool imageSearchTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(imageSearchTool)
                .build();
    }

}
