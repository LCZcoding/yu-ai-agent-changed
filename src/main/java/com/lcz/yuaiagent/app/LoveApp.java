package com.lcz.yuaiagent.app;

import com.lcz.yuaiagent.advisor.MyLoggerAdvisor;
import com.lcz.yuaiagent.advisor.ReReadingAdvisor;
import com.lcz.yuaiagent.chatmemory.FileBasedChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 恋爱心理专家对话应用。
 *
 * <p>封装了与 AI 大模型交互的完整流程，包括系统提示词、会话记忆和增强器链。
 * 通过 {@link #doChat(String, String)} 方法发送用户消息并获取 AI 回复。
 */
@Component
@Slf4j
public class LoveApp {

    /** 对话客户端，封装了系统提示词、会话记忆以及底层大模型调用。 */
    private final ChatClient chatClient;

    /** 系统提示词：定义 AI 的角色身份、提问策略及引导方式。 */
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。\n";

    /**
     * 构造方法，初始化对话客户端与会话记忆。
     *
     * @param dashScopeChatModel 底层大语言模型（通义千问 DashScope）
     */
    public LoveApp(ChatModel dashScopeChatModel) {
        //  初始化基于文件的会话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";


        // 创建基于文件的会话记忆，最多保留最近 10 条消息
        ChatMemory chatMemory = MessageWindowChatMemory
                .builder()                                          // 创建 MessageWindowChatMemory 构建器
                .chatMemoryRepository(new FileBasedChatMemory(fileDir))  // 设置file存储仓库
                .maxMessages(10)                                    // 设置最大保留消息数
                .build();
//        // 创建基于内存的会话记忆，最多保留最近 10 条消息
//        ChatMemory chatMemory = MessageWindowChatMemory
//                .builder()                                          // 创建 MessageWindowChatMemory 构建器
//                .chatMemoryRepository(new InMemoryChatMemoryRepository())  // 设置内存存储仓库
//                .maxMessages(10)                                    // 设置最大保留消息数
//                .build();                                           // 构建 ChatMemory 实例

        // 创建对话客户端
        chatClient = ChatClient.builder(dashScopeChatModel)         // 指定底层大模型
                .defaultSystem(SYSTEM_PROMPT)                       // 设置系统提示词，定义 AI 角色和行为
                .defaultAdvisors(
                    MessageChatMemoryAdvisor
                            .builder(chatMemory)                    // 传入记忆对象，自动附加历史消息
                            .build(),
                    new MyLoggerAdvisor()
//                        ,new ReReadingAdvisor()
                )
                .build();                                           // 构建 ChatClient 实例
    }

    /**
     * 执行聊天对话。
     *
     * @param message 用户输入的消息内容
     * @param chatId  聊天会话的唯一标识符，用于关联历史消息
     * @return AI 助手的回复内容；如果响应为空则返回 {@code null}
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()             // 开始构建一次对话请求
                .user(message)                                      // 设置用户发送的消息内容
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))  // 传入会话 ID，关联历史记忆
                .call()                                             // 执行同步调用（非流式）
                .chatResponse();                                    // 获取完整的 ChatResponse 对象

        String content = null;
        if (chatResponse != null) {
            content = chatResponse.getResult().getOutput().getText();  // 获取第一个结果中 AI 回复的文本
        }

        return content;
    }

    record LoveReport(String title, List<String> suggestions) {

    }

    /**
     * AI恋爱报告，结构化输出
     *
     * @param message 用户输入的消息内容
     * @param chatId  聊天会话的唯一标识符，用于关联历史消息
     * @return AI 助手的回复内容；如果响应为空则返回 {@code null}
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient.prompt()             // 开始构建一次对话请求
                .system(SYSTEM_PROMPT + "请结构化输出恋爱报告，标题为用户名，内容为建议列表")
                .user(message)                                      // 设置用户发送的消息内容
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))  // 传入会话 ID，关联历史记忆
                .call()                                             // 执行同步调用（非流式）
                .entity(LoveReport.class);                          //  解析响应为 LoveReport 对象
        //log.info("loveReport: {}", loveReport);
        return loveReport;
    }
}