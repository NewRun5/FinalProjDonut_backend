package com.donut.refrenceOnly.contentChatBot.langGraph;

import com.donut.common.search.DocumentSearchService;
import com.donut.common.search.ImageSearchService;
import com.donut.common.utils.ChatBotComponent;
import com.donut.common.utils.PromptLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final ChatBotComponent component;
    private final PromptLoader promptLoader = new PromptLoader("content/");
    private final ImageSearchService imageSearchService;


    public List<String> genSearchQuery(String userQuery) {
        String prompt = promptLoader.get("genSearchQuery");
        System.out.println(prompt);
        System.out.println(userQuery);
        List<String> result = (List<String>) component.getStructuredOutput(prompt, userQuery, List.class);
        return result;
    }

    public List<Map<String, Object>> hybridSearchQuery(List<String> queryList) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String query : new ArrayList<>(queryList)) {
            System.out.println("쿼리 " + query);
            List<Map<String, Object>> searchResult = imageSearchService.hybridSearch(query);
            result.addAll(searchResult);
        }
        return result;
    }

    public String genHTML(List<Map<String, Object>> documents, String userQuery) {
        String prompt = promptLoader.get("HTML", Map.of("context", documents.toString()));
        String document = component.getChatResponseWithSysMsg(prompt, userQuery).getContent();
        return document;
    }
}
