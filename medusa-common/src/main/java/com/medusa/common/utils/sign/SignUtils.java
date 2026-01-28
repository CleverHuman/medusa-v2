package com.medusa.common.utils.sign;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.TreeMap;

public class SignUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 规范化JSON body：排序+无空格
     */
    public static String normalizeJson(String jsonBody) throws IOException, JsonProcessingException {
        // 把 JSON 反序列化为 TreeMap (自动按key排序)
        ObjectNode objectNode = (ObjectNode) mapper.readTree(jsonBody);
        TreeMap<String, Object> sortedMap = new TreeMap<>();
        objectNode.fields().forEachRemaining(entry -> sortedMap.put(entry.getKey(), entry.getValue()));

        // 重新序列化，去掉所有缩进和空格
        ObjectMapper cleanMapper = new ObjectMapper();
        cleanMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true); // 按key序
        return cleanMapper.writeValueAsString(sortedMap);
    }
}
