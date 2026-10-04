package com.lcz.imagessearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UnsplashImageTool {

        // Unsplash 提供的 API Access Key
        @Value("${unsplashAPIKey.apiKey}")
        private String accessKey;

    @Tool(description = "search image from web")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        try {
            return String.join(",", searchMediumImages(query));
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }



    public List<String> searchMediumImages(String query) throws IOException, InterruptedException {
        // 中文需要 URL 编码，不然会报错
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        // 按照文档拼接 URL
        String url = "https://api.unsplash.com/search/photos"
                + "?query=" + encodedQuery
                + "&per_page=10"
                + "&client_id=" + accessKey;

        // Java 标准写法，发 HTTP 请求
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept-Version", "v1") // 官方建议加上的版本号
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return JSONUtil.parseObj(response.body())
                .getJSONArray("results") // 1. Unsplash 的数组叫 results
                .stream()
                .map(photoObj -> (JSONObject) photoObj)
                .map(photoObj -> photoObj.getJSONObject("urls")) // 2. Unsplash 的图片链接对象叫 urls
                .map(urls -> urls.getStr("regular")) // 3. Unsplash 没有 medium，对应的是 regular (或 small)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());

//        return JSONUtil.parseObj(response.body())
//                .getJSONArray("photos")
//                .stream()
//                .map(photoObj -> (JSONObject) photoObj)
//                .map(photoObj -> photoObj.getJSONObject("src"))
//                .map(photo -> photo.getStr("medium"))
//                .filter(StrUtil::isNotBlank)
//                .collect(Collectors.toList());
    }

}