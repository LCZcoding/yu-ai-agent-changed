package com.lcz.imagessearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class UnsplashDemoTest {
    @Resource
    UnsplashImageTool unsplashImageTool;

    @Test
    void searchImage() {
        String result = unsplashImageTool.searchImage("cat");
        System.out.println(result);
        assertTrue(StrUtil.isNotBlank(result));
    }
}