package com.lcz.yuaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

/**
 * {@link LoveApp} 的集成测试，验证多轮对话的会话记忆功能。
 */
@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    /**
     * 测试多轮对话：自我介绍 → 身份回忆 → 问题咨询。
     */
    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();

        String message = "你好,我是张三";
        String answer = loveApp.doChat(message, chatId);

        message = "我是谁";
        answer = loveApp.doChat(message, chatId);

        message = "如何脱单";
        answer = loveApp.doChat(message, chatId);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();

        String message = "你好,我是张三,我想让另一半更爱我，我该怎么做？";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);

    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我单身，如何脱单？";
        String answer = loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }
}