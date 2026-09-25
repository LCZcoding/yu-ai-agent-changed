package com.lcz.yuaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好,我是张三";
        String answer = loveApp.doChat(message, chatId);

        // 第二轮
        message = "我是谁";
        answer = loveApp.doChat(message, chatId);

        // 第三轮
        message = "如何脱单";
        answer = loveApp.doChat(message, chatId);
    }
}