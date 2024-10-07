package com.donut.common.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Component
public class JsonUtil {
    private final Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdaptor())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdaptor())
                .create();

    public String jsonStringify(Object obj){
        return gson.toJson(obj);
    }
}
