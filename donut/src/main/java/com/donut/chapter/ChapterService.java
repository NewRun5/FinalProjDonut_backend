package com.donut.chapter;

import com.donut.chapter.langGraph.ChapterContentState;
import com.donut.chapter.langGraph.ChapterLangGraph;
import com.donut.chapter.questionChatBot.Chap;
import com.donut.common.gson.JsonUtil;
import com.donut.curriculum.CurriculumDTO;
import lombok.RequiredArgsConstructor;
import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.NodeOutput;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
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
        CurriculumDTO curriculumDTO = mapper.getCurriculumByChapterId(chapterId);
        List<ChapterDTO> chapterList = curriculumDTO.getChapterList();
        ChapterDTO currentChapter = chapterList
                .stream()
                .filter(chap->chap.getId() == chapterId)
                .findFirst()
                .orElse(new ChapterDTO());

        if (currentChapter.getContent() != null) {
            return currentChapter.getContent();
        }
        LocalDate current = LocalDate.now();
        int createDateResult = mapper.updateCreateDate(Map.of("currentTime", current, "chapterId", chapterId));
        String beforeContent = "이전 챕터가 없습니다. 즉 첫 번째 챕터입니다.";
        int currentIndex = chapterList.indexOf(currentChapter);
        if (currentIndex > 0) beforeContent = chapterList.get(currentIndex - 1).getContent();
        System.out.println(beforeContent);

        AsyncGenerator<NodeOutput<ChapterContentState>> graph = null;
        try {
            graph = langGraph.buildGraph()
                    .compile()
                    .stream(mapOf("chapter", currentChapter,
                            "beforeContent", beforeContent));
        } catch (Exception e) {
            e.printStackTrace();
            return "문서 생성에 실패하였습니다.";
        }
        String result = "";
        for (var i : graph) {
            result = i.state().genContent();
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

    public List<Chap> getAllChapterByUserId(String userId) {
        return mapper.getAllChapterByUserId(userId);
    }
}
