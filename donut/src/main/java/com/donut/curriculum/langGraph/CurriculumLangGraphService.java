package com.donut.curriculum.langGraph;

import com.donut.common.gson.JsonUtil;
import com.donut.common.search.DocumentSearchService;
import com.donut.common.utils.ChatBotComponent;
import com.donut.common.utils.ChatBotMemory;
import com.donut.common.utils.PromptLoader;
import com.donut.curriculum.langGraph.model.ChatHistory;
import com.donut.curriculum.langGraph.model.UserNeeds;
import com.donut.curriculum.langGraph.model.Curriculum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurriculumLangGraphService {
    private final PromptLoader promptLoader = new PromptLoader("curriculum/");
    private final JsonUtil jsonUtil;
    private final ChatBotComponent component;
    private final DocumentSearchService searchService;
    public UserNeeds isEnoughUserInput(List<ChatHistory> chatHistoryList) {

        String prompt = promptLoader.get("isEnoughUserInput");

        ChatBotMemory memory = new ChatBotMemory(prompt);
        memory.convert(chatHistoryList);

        return (UserNeeds) component.getStructuredOutputByMemory(memory, UserNeeds.class);
    }

    public String needMoreInput(List<ChatHistory> chatHistoryList, UserNeeds userNeeds) {
        String prompt = promptLoader.get("needMoreInput", Map.of("context", jsonUtil.jsonStringify(userNeeds)));
        ChatBotMemory memory = new ChatBotMemory(prompt);
        memory.convert(chatHistoryList);
        return component.getChatResponseByMemory(memory).getContent();
    }

    public List<String> generateSearchQuery(List<ChatHistory> chatHistoryList) {
        String prompt = promptLoader.get("generateSearchQuery");
        ChatBotMemory memory = new ChatBotMemory(prompt);
        memory.convert(chatHistoryList);
        String[] array = (String[]) component.getStructuredOutputByMemory(memory, String[].class);
        return Arrays.asList(array);
    }

    public List<Map<String, Object>> searchDocument(String query) {
        List<Map<String, Object>> result = searchService.hybridSearch(query);
        List<Map<String, Object>> result2 = result.stream().filter(m -> (double)m.get("score") > 0.75).toList();
        System.out.println("검색 결과 수" + result.size());
        System.out.println("필터링 결과" + result2.size());
        return result2;
    }

    public String isUsefulData(List<Map<String, Object>> documents, List<ChatHistory> chatHistoryList) {
        String prompt = promptLoader.get("isUsefulData", Map.of("context", jsonUtil.jsonStringify(documents)));
        ChatBotMemory memory = new ChatBotMemory(prompt);
        memory.convert(chatHistoryList);
        String result = component.getStructuredOutputByMemory(memory, boolean.class) + "";
        if (result.equals("true")) {
            return "true";
        }
        return "false";
    }

    public Curriculum generateCurriculumUseRag(List<Map<String, Object>> documents, List<ChatHistory> chatHistoryList) {
        String prompt = promptLoader.get("generateCurriculumUseRag", Map.of("context", jsonUtil.jsonStringify(documents)));
        ChatBotMemory memory = new ChatBotMemory(prompt);
        memory.convert(chatHistoryList);
        Curriculum result = (Curriculum)component.getStructuredOutputByMemory(memory, Curriculum.class);
        System.out.println(result);
        return result;
    }

    public Curriculum generateCurriculumNotUseRag(List<ChatHistory> chatHistoryList) {
        String prompt = promptLoader.get("generateCurriculumNotUseRag");
        ChatBotMemory memory = new ChatBotMemory(prompt);
        memory.convert(chatHistoryList);
        Curriculum result = (Curriculum) component.getStructuredOutputByMemory(memory, Curriculum.class);
        System.out.println(result);
        return result;
    }
}
