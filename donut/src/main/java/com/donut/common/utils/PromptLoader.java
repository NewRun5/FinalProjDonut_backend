package com.donut.common.utils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

public class PromptLoader {
    private final String base;

    public PromptLoader(String base) {
        this.base =  "prompts/" + base;
    }

    public String get(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(base + path + ".txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public String get(String path, Map<String, String> replacements) {
        try {
            ClassPathResource resource = new ClassPathResource(base + path + ".txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            // 파일의 내용을 String으로 읽어옴
            String fileContent = reader.lines().collect(Collectors.joining("\n"));

            // Map을 순회하면서 #{key}를 value로 치환
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                String placeholder = "#{" + entry.getKey() + "}";  // 예시: #{question}
                fileContent = fileContent.replace(placeholder, entry.getValue());
            }

            return fileContent;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}