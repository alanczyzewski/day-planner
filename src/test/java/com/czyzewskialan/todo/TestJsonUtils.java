package com.czyzewskialan.todo;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class TestJsonUtils {

    public static String convertObjectToJson(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
