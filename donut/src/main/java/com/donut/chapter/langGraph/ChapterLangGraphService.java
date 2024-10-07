package com.donut.chapter.langGraph;

import com.donut.chapter.ChapterDTO;
import com.donut.common.gson.JsonUtil;
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
public class ChapterLangGraphService {
    private final ChatBotComponent component;
    private final PromptLoader promptLoader = new PromptLoader("chapter/content/");
    private final JsonUtil jsonUtil;
    private final DocumentSearchService documentSearchService;
    private final ImageSearchService imageSearchService;
    public String[] genDocumentSearchQuery(ChapterDTO chapter) {
        String chapterDescription = "";
        chapterDescription = "{title : " + chapter.getTitle()  + "}"+
                "{description : " + chapter.getDescription()  + "}"+
                "{goal : " + chapter.getGoal()  + "}";
        String prompt = promptLoader.get("genDocumentSearchQuery");
        String [] result = (String[]) component.getStructuredOutput(prompt, chapterDescription, String[].class);
        return result;
    }

    public List<Map<String, Object>> documentSearch(String[] searchQuery) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String query : searchQuery) {
            List<Map<String, Object>> searchResult = documentSearchService.hybridSearch(query);
            result.addAll(searchResult);
        }
        result = result.stream().filter(r-> (double)r.get("score") > 0.75).toList();
        System.out.println("필터링 결과 문서 : " + result.size());
        return result;
    }

    public String genContentPrototype(List<Map<String, Object>> documentList, ChapterDTO chapter) {
        String context;
        if(documentList.size() == 0){
            context = "주어진 정보가 없습니다.";
        } else {
            context = jsonUtil.jsonStringify(documentList);
        }
        String prompt = promptLoader.get("genContentPrototype", Map.of("context", context));

        String chapterDescription = "{챕터 이름 : " + chapter.getTitle() + "}"+
                "{설명 : " + chapter.getDescription()  + "}"+
                "{학습 목표 : " + chapter.getGoal()  + "}";
        String result = component.getChatResponseWithSysMsg(prompt, chapterDescription).getContent();
        System.out.println(result);
        return result;
    }

    public String[] genSearchImgQuery(String contentPrototype) {
        String prompt = promptLoader.get("genSearchImgQuery");
        String[] result = (String[]) component.getStructuredOutput(prompt, contentPrototype, String[].class);
        System.out.println(result.length);
        return result;
    }

    public List<Map<String, Object>> imageSearch(String[] searchImgQuery) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String query : searchImgQuery) {
            System.out.println("쿼리 " + query);
            List<Map<String, Object>> searchResult = imageSearchService.hybridSearch(query);
            result.addAll(searchResult);
        }
        result = result.stream().filter(r-> (double)r.get("score") > 0.75).toList();
        System.out.println("필터링 결과 이미지 : " + result.size());
        return result;
    }

    public String genContent(ChapterDTO chapter, List<Map<String, Object>> imageList, List<Map<String, Object>> documentList) {
        String chapterDescription =  "{챕터 이름 : " + chapter.getTitle() + "},"+
                "{설명 : " + chapter.getDescription()  + "},"+
                "{학습 목표 : " + chapter.getGoal()  + "}";
        String prompt = promptLoader.get("genContent", Map.of("chapter", chapterDescription));

        String imageString = jsonUtil.jsonStringify(imageList);
        String documentString = jsonUtil.jsonStringify(documentList);
        String userQuery = "{문서 정보 : " + documentString + "}, {이미지 정보 :" + imageString + "}";

        String result = component.getChatResponseWithSysMsg(prompt, userQuery).getContent();
        System.out.println(result);
        return result;
    }
}
