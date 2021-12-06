package com.murphyl.etl.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    };

    public abstract <T> String stringify(T payload);

    public abstract <T> T parse(String text, Class<T> tClass);

}
