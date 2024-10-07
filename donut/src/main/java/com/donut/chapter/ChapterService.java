package com.donut.chapter;

import com.donut.chapter.langGraph.ChapterContentState;
import com.donut.chapter.langGraph.ChapterLangGraph;
import lombok.RequiredArgsConstructor;
import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.NodeOutput;
import org.springframework.stereotype.Service;

import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@Service
@RequiredArgsConstructor
public class ChapterService {
    private final ChapterMapper mapper;
    private final ChapterLangGraph langGraph;


    public String getChapterContentById(int chapterId) {
        ChapterDTO chapterDTO = mapper.getChapterById(chapterId);

        if(chapterDTO.getContent() != null) {
            return chapterDTO.getContent();
        }

        AsyncGenerator<NodeOutput<ChapterContentState>> result = null;
        try {
            result = langGraph.buildGraph().compile().stream(mapOf("chapter", chapterDTO));
        } catch (Exception e) {
            return "문서 생성에 실패하였습니다.";
        }

        for (var i : result){
        }
        return "";
    }
}
