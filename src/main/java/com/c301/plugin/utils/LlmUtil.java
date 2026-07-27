package com.c301.plugin.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * LLM工具类，用于处理与AI模型的交互
 *
 * @author Chenbing
 * @version 1.0
 */
public class LlmUtil {
    private static final String API_KEY = "";
    private static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    private static final String MODEL = "qwen-max-latest";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void streamChat(String userMessage, StreamResponseHandler handler) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(BASE_URL + "/chat/completions");

            // 设置请求头
            httpPost.setHeader("Authorization", "Bearer " + API_KEY);
            httpPost.setHeader("Content-Type", "application/json");

            // 构建请求体
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "You are a helpful assistant."));
            messages.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> requestBody = Map.of(
                    "model", MODEL,
                    "messages", messages,
                    "stream", true,
                    "stream_options", Map.of("include_usage", true)
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            // 执行请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String jsonData = line.substring(6);
                                if (jsonData.equals("[DONE]")) {
                                    handler.onComplete();
                                    break;
                                }
                                handler.onMessage(jsonData);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            handler.onError(e);
        }
    }

    public static void main(String[] args) {
        // 测试示例
        streamChat("你是谁？", new StreamResponseHandler() {
            @Override
            public void onMessage(String message) {
                System.out.println("收到消息: " + message);
            }

            @Override
            public void onComplete() {
                System.out.println("流式响应完成");
            }

            @Override
            public void onError(Exception e) {
                System.err.println("发生错误: " + e.getMessage());
            }
        });
    }

    public interface StreamResponseHandler {
        void onMessage(String message);

        void onComplete();

        void onError(Exception e);
    }
}