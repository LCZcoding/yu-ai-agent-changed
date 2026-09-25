/*
 * Copyright 2023-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lcz.yuaiagent.advisor;

import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;

/**
 * 日志增强器，用于记录 AI 请求与响应的日志信息。
 *
 * <p>该类同时实现了 {@link CallAdvisor} 和 {@link StreamAdvisor} 接口，
 * 可在同步调用和流式调用场景下，分别在请求发送前和响应返回后输出日志。
 * 日志内容包含用户消息文本和 AI 响应文本。
 *
 * @author lcz
 */
@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

	/** 增强器的执行顺序。 */
	private final int order = 0;

	/**
	 * 同步调用增强：记录请求日志后执行下一个增强器，再记录响应日志。
	 *
	 * @param chatClientRequest 当前请求
	 * @param callAdvisorChain  增强器调用链
	 * @return 包含响应结果的 {@link ChatClientResponse}
	 */
	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		logRequest(chatClientRequest);

		ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

		logResponse(chatClientResponse);

		return chatClientResponse;
	}

	/**
	 * 流式调用增强：记录请求日志后执行下一个流式增强器。
	 * <p>响应日志在流式数据聚合完成后统一记录，而非每条消息单独记录。
	 *
	 * @param chatClientRequest   当前请求
	 * @param streamAdvisorChain  流式增强器调用链
	 * @return 聚合后的流式响应 Flux
	 */
	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
			StreamAdvisorChain streamAdvisorChain) {
		logRequest(chatClientRequest);

		Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);

		return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);
	}

	/**
	 * 记录请求日志。
	 * <p>提取用户消息文本并输出到日志。
	 *
	 * @param request 要记录的请求
	 */
	protected void logRequest(ChatClientRequest request) {
		log.info("AI request: {}", request.prompt().getUserMessage());
	}

	/**
	 * 记录响应日志。
	 * <p>提取 AI 响应文本并输出到日志。
	 *
	 * @param chatClientResponse 要记录的响应
	 */
	protected void logResponse(ChatClientResponse chatClientResponse) {
		log.info("AI response: {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
	}

	/**
	 * 返回增强器的名称。
	 *
	 * @return 类名
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * 返回增强器的执行顺序。
	 *
	 * @return 顺序值，数值越小优先级越高
	 */
	@Override
	public int getOrder() {
		return this.order;
	}

}