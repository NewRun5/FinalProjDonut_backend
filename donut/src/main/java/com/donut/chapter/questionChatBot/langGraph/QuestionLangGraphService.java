package com.donut.chapter.questionChatBot.langGraph;

import com.donut.chapter.questionChatBot.ChatHistoryDTO;
import com.donut.common.gson.JsonUtil;
import com.donut.common.search.DocumentSearchService;
import com.donut.common.search.ImageSearchService;
import com.donut.common.utils.ChatBotComponent;
import com.donut.common.utils.ChatBotMemory;
import com.donut.common.utils.PromptLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuestionLangGraphService {
    private final ChatBotComponent component;
    private final DocumentSearchService documentSearchService;
    private final ImageSearchService imageSearchService;
    private final PromptLoader promptLoader = new PromptLoader("question/");
    private final JsonUtil jsonUtil;
    public SearchQuery genSearchQuery(List<ChatHistoryDTO> chatHistoryList) {
        String prompt = promptLoader.get("genSearchQuery");
        ChatBotMemory memory = ChatBotMemory.from(chatHistoryList);
        memory.getHistory().add(0, new SystemMessage(prompt));
        SearchQuery result = component.getStructuredOutputByMemory(memory, SearchQuery.class);
        return result;
    }

    public List<Map<String, Object>> documentSearch(List<String> documentSearchQueryList) {
        List<Map<String, Object>> result = new ArrayList<>();
        documentSearchQueryList.forEach(query->{
            result.addAll(documentSearchService.hybridSearch(query));
        });
        List<Map<String, Object>> filterResult = result.stream().filter(doc -> (double)doc.get("score") > 0.9).toList();
        return filterResult;
    }

    public List<Map<String, Object>> imageSearch(List<String> imageSearchQuery) {
        List<Map<String, Object>> result = new ArrayList<>();
        imageSearchQuery.forEach(query->{
            result.addAll(imageSearchService.hybridSearch(query));
        });
        List<Map<String, Object>> filterResult = result.stream().filter(doc -> (double)doc.get("score") > 0.85).toList();
        return filterResult;
    }

    public String genAnswer(List<ChatHistoryDTO> chatHistoryList, List<Map<String, Object>> documentResult, List<Map<String, Object>> imageResult) {
        String prompt = promptLoader.get("genAnswer", Map.of("documentList", jsonUtil.jsonStringify(documentResult), "imageList", jsonUtil.jsonStringify(imageResult)));
        ChatBotMemory memory = ChatBotMemory.from(chatHistoryList);
        System.out.println(prompt);
        System.out.println(memory.getHistory().size());
        String result = component.getChatResponseByMemoryWithSysMsg(memory, prompt).getContent();
        return result;
    }
}
