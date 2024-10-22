package com.donut.chapter.langGraph;

import com.donut.chapter.ChapterDTO;
import com.donut.chapter.langGraph.model.SearchQueries;
import com.donut.chapter.langGraph.model.SelfFeedback;
import com.donut.common.gson.JsonUtil;
import com.donut.common.search.DocumentSearchService;
import com.donut.common.search.ImageSearchService;
import com.donut.common.utils.ChatBotComponent;
import com.donut.common.utils.ChatBotMemory;
import com.donut.common.utils.PromptLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChapterLangGraphService {
    private final ChatBotComponent component;
    private final PromptLoader promptLoader = new PromptLoader("chapter/content/");
    private final JsonUtil jsonUtil;
    private final DocumentSearchService documentSearchService;
    private final ImageSearchService imageSearchService;

    public SearchQueries genSearchQueries(ChapterDTO chapter) {
        String prompt = promptLoader.get("genSearchQueries");
        String query = jsonUtil.jsonStringify(chapter);
        SearchQueries result = component.getStructuredOutput(prompt, query, SearchQueries.class);
        return result;
    }

    public List<Map<String, Object>> searchDocument(List<String> documentSearchQuery) {
        List<Map<String, Object>> result = new ArrayList<>();
        documentSearchQuery.forEach(query->{
            List<Map<String, Object>> searchResult = documentSearchService.hybridSearch(query);
            result.addAll(searchResult);
        });
        List<Map<String, Object>> filterResult = result.stream().filter(doc -> (double)doc.get("score") > 0.9).toList();
        System.out.println("문서 검색 결과: " + filterResult.size());
        return filterResult;
    }

    public List<Map<String, Object>> searchImage(List<String> documentSearchQuery) {
        List<Map<String, Object>> result = new ArrayList<>();
        documentSearchQuery.forEach(query->{
            List<Map<String, Object>> searchResult = imageSearchService.hybridSearch(query);
            result.addAll(searchResult);
        });
        List<Map<String, Object>> filterResult = result.stream().filter(doc -> (double)doc.get("score") > 0.7).toList();
        System.out.println("이미지 검색 결과: " + filterResult.size());
        return filterResult;
    }

    public String genContent(ChapterDTO chapter, String beforeContent, List<Map<String, Object>> documentSearchResult, List<Map<String, Object>> imageSearchResult) {
        String prompt = promptLoader.get("genContent", Map.of("chapter", jsonUtil.jsonStringify(chapter)));
        String before;
        if(beforeContent == null) beforeContent = "첫 번째 챕터입니다";
        String query = "###문서 자료### \n" + jsonUtil.jsonStringify(documentSearchResult) +
                " \n ### 이미지 자료 ### \n" + jsonUtil.jsonStringify(imageSearchResult) +
                " \n ###이전 챕터 내용### \n" + beforeContent;
        String result = component.getChatResponseWithSysMsg(prompt, query).getContent();
        return result;
    }

    public SelfFeedback selfTest(String genContent, String beforeContent, ChapterDTO chapter) {
        if(beforeContent == null) beforeContent = "첫 챕터입니다.";
        String prompt = promptLoader.get("selfTest", Map.of("chapter", jsonUtil.jsonStringify(chapter),
                "beforeChapter", beforeContent));
        String query = "###생성된 내용###\n" + genContent;
        ChatBotMemory memory = new ChatBotMemory(prompt);
        memory.save(new UserMessage(query));
        SelfFeedback feedback = component.getStructuredOutputByMemory(memory, SelfFeedback.class);
        System.out.println(feedback.getComment());
        System.out.println(feedback.getScore());
        return feedback;
    }

    public String genContentByFeedback(ChapterDTO chapter, String beforeContent, List<Map<String, Object>> documentSearchResult, List<Map<String, Object>> imageSearchResult, String content, SelfFeedback feedback) {
        if(beforeContent == null) beforeContent = "첫 챕터입니다.";
        String prompt = promptLoader.get("genContentByFeedback", Map.of("chapter", jsonUtil.jsonStringify(chapter),
                "beforeContent", beforeContent,
                "feedback", feedback.getComment(),
                "doc", jsonUtil.jsonStringify(documentSearchResult),
                "image", jsonUtil.jsonStringify(imageSearchResult)));
        String query = promptLoader.get("genContentInput", Map.of("content", content));
        String result = component.getChatResponseWithSysMsg(prompt, query).getContent();
        return result;
    }
}
