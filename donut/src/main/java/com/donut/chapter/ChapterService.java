package com.donut.chapter;

import com.donut.chapter.langGraph.ChapterContentState;
import com.donut.chapter.langGraph.ChapterLangGraph;
import com.donut.common.gson.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.NodeOutput;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@Service
@RequiredArgsConstructor
public class ChapterService {
    private final ChapterMapper mapper;
    private final ChapterLangGraph langGraph;
    private final OpenAiImageModel openAiImageModel;
    private final JsonUtil jsonUtil;


    public String getChapterContentById(int chapterId) {
        ChapterDTO chapterDTO = mapper.getChapterById(chapterId);


        if (chapterDTO.getContent() != null) {
            return chapterDTO.getContent();
        }

        AsyncGenerator<NodeOutput<ChapterContentState>> graph = null;
        try {
            graph = langGraph.buildGraph().compile().stream(mapOf("chapter", chapterDTO));
        } catch (Exception e) {
            return "문서 생성에 실패하였습니다.";
        }
        String result = "";
        for (var i : graph) {
            result = i.state().content();
        }
        int insertResult = mapper.updateChapterContent(Map.of(
                "chapterId", chapterId,
                "content", result));
        if (insertResult > 0) {
            return result;
        } else {
            return "오류 발생";
        }
    }

    @Transactional
    public LocalDate updateCompleteDate(int chapterId) {
        LocalDate current = LocalDate.now();
        int result = mapper.updateCompleteDate(Map.of("currentTime", current, "chapterId",chapterId));
        if (result > 0) {
            return current;
        } else {
            throw new IllegalStateException("업데이트 실패");
        }
    }
}
