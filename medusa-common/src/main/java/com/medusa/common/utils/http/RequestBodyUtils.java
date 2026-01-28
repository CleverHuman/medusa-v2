package com.medusa.common.utils.http;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RequestBodyUtils {

    /**
     * 读取HttpServletRequest的请求体(JSON字符串)
     */
    public static String readJsonBody(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line.trim()); // 注意：trim每行去头尾空格
            }
        }
        return stringBuilder.toString();
    }
}

