package com.lcz.yuaiagent.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class LoveApp {

    /** 对话客户端，封装了系统提示词、会话记忆以及底层大模型调用 */
    private final ChatClient chatClient;

    /** 系统提示词：定义 AI 的角色身份、提问策略及引导方式 */
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。\n";

    /**
     * 构造方法，初始化对话客户端与会话记忆。
     * <p>
     * 使用基于内存的消息窗口记忆，最多保留最近 10 条消息，
     * 并通过 {@link MessageChatMemoryAdvisor} 将会话记忆注入到每次对话中。
     *
     * @param dashScopeChatModel 底层大语言模型（通义千问 DashScope）
     */
    public LoveApp(ChatModel dashScopeChatModel){

        // 构造基于内存的记忆对象
        ChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();

        // 构造对话客户端，设置系统提示词、记忆
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                    MessageChatMemoryAdvisor
                            .builder(chatMemory)
                            .build()
                )
                .build();
    }
/**
 * 执行聊天对话的方法
 * @param message 用户输入的消息内容
 * @param chatId 聊天会话的唯一标识符
 * @return 返回AI助手的回复内容
 */
    public String doChat(String message, String chatId){
        // 使用chatClient创建聊天提示
        ChatResponse chatResponse = chatClient.prompt()
                // 设置用户消息
                .user(message)
                // 配置聊天顾问，设置会话ID以维护聊天记忆
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                // 执行聊天调用
                .call()
                // 获取聊天响应对象
                .chatResponse();
        // 从响应结果中提取文本内容
        String content = null;
        if (chatResponse != null) {
            content = chatResponse.getResult().getOutput().getText();
        }
        // 记录日志，输出聊天内容
        log.info("content:{}",content);
        // 返回聊天内容
        return content;
    }
}