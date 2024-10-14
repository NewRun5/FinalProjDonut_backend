package com.donut.curriculum.langGraph;

import com.donut.common.gson.JsonUtil;
import com.donut.common.search.CurriculumSearchService;
import com.donut.common.search.DocumentSearchService;
import com.donut.common.utils.ChatBotComponent;
import com.donut.common.utils.ChatBotMemory;
import com.donut.common.utils.PromptLoader;
import com.donut.curriculum.langGraph.model.GenCurriculum;
import com.donut.curriculum.langGraph.model.InputComment;
import com.donut.curriculum.langGraph.model.SerializableMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private final CurriculumSearchService curriculumSearchService;

    public String isWantNewCurriculum(List<SerializableMemory> memory) {
        String result = "";
        String prompt = promptLoader.get("isWantNewCurriculum");
        int parseTryCount = 0;
        while (parseTryCount < 7) {
            result = component.getChatResponseByMemoryWithSysMsg(ChatBotMemory.of(memory), prompt).getContent();
            if(result.equals("true")) return result;
            if(result.equals("false")) return result;
            parseTryCount++;
        }
        System.out.println("파싱실패");
        return "false";
    }

    public GenCurriculum fixCurriculum(List<SerializableMemory> chatHistory) {
        String prompt = promptLoader.get("fixCurriculum");
        GenCurriculum genCurriculum = component.getStructuredOutputByMemory(ChatBotMemory.of(chatHistory), prompt, GenCurriculum.class);
        return genCurriculum;
    }

    public InputComment genInputComment(List<SerializableMemory> chatHistory) {
        String prompt = promptLoader.get("genInputComment");
        ChatBotMemory memory = ChatBotMemory.of(chatHistory);
        memory.getHistory().add(0, new SystemMessage(prompt));
        InputComment result = component.getStructuredOutputByMemory(memory, InputComment.class);
        return result;
    }

    public List<Map<String, Object>> hybridSearch(List<SerializableMemory> chatHistory) {
        String prompt = promptLoader.get("genSearchQuery");
        ChatBotMemory memory = ChatBotMemory.of(chatHistory);
        memory.getHistory().add(0, new SystemMessage(prompt));
        String[] searchQuery = component.getStructuredOutputByMemory(memory, String[].class);
        List<Map<String, Object>> result = new ArrayList<>();
        Arrays.stream(searchQuery).forEach(query->{
            List<Map<String, Object>> searchResult = curriculumSearchService.hybridSearch(query);
            result.addAll(searchResult);
        });
        System.out.println("참고용 커리큘럼" + result.size());
        return result;
    }

    public GenCurriculum genCurriculum(List<Map<String, Object>> documentList, List<SerializableMemory> chatHistory) {
        String prompt = promptLoader.get("genCurriculum", Map.of("context", jsonUtil.jsonStringify(documentList)));
        ChatBotMemory memory = ChatBotMemory.of(chatHistory);
        memory.getHistory().add(0, new SystemMessage(prompt));
        GenCurriculum result = component.getStructuredOutputByMemory(memory, GenCurriculum.class);
        return result;
    }
}
