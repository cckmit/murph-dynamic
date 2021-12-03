package com.murphyl.etl.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 序列化 - 工具类
 *
 * @date: 2021/12/3 13:38
 * @author: murph
 */
public enum Serializers {

    JSON() {

        private final ObjectMapper MAPPER = new ObjectMapper();

        @Override
        public <T> String stringify(T payload) {
            try {
                return MAPPER.writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("use [json] serialize data error", e);
            }
        }

        @Override
        public <T> T parse(String text, Class<T> tClass) {
            try {
                return MAPPER.readValue(text, tClass);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("use [json] parse data error", e);
            }
        }

        @Override
        public <T> T validateAndParse(JsonSchema schemaValidator, String text, Class<T> tClass) {
            try {
                JsonNode parsed = MAPPER.readTree(text);
                Set<ValidationMessage> result = schemaValidator.validate(parsed);
                if (null != result && !result.isEmpty()) {
                    throw new IllegalStateException(result.stream().map(item -> item.getMessage()).collect(Collectors.joining(", ")));
                }
                return MAPPER.treeToValue(parsed, tClass);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("use [json] parse data error", e);
            } catch (IllegalStateException e) {
                throw new IllegalStateException("use [json-schema] validate data error", e);
            } catch (Exception e) {
                throw new IllegalStateException("use [json-schema] validate and parse data error", e);
            }
        }
    };

    public abstract <T> String stringify(T payload);

    public abstract <T> T parse(String text, Class<T> tClass);

    public abstract <T> T validateAndParse(JsonSchema schemaValidator, String text, Class<T> tClass);

}
