package com.lcz.yuaiagent.tools;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistration {

    @Value("${tavily.api-key}")
    private String tavilyApiKey;

    // 工厂模式 注册所有工具,隐藏实现细节
    // 依赖注入模式 注入 tavilyApiKey
    // 注册模式 集中注册和管理所有工具到 Spring 容器中
    // 适配器模式 适配不同工具的接口,使它们能够统一处理
    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(tavilyApiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        // ToolCallbacks.from() 会扫描每个传入对象的 @Tool 方法，为每个方法创建一个 MethodToolCallback 对象
        return ToolCallbacks.from( // 适配器 将所有工具适配为 ToolCallback[] 类型
            fileOperationTool,
            webSearchTool,
            webScrapingTool,
            resourceDownloadTool,
            terminalOperationTool,
            pdfGenerationTool
        );
    }
}
