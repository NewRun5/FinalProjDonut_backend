package com.donut.chapter;

import com.donut.chapter.questionChatBot.Chap;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService service;
    private final Map<Integer, Boolean> waitingGenerateMap = new ConcurrentHashMap<>();
    private final HttpSession session; // HttpSession 주입
    @QueryMapping
    public String getChapterContentById(@Argument int chapterId){
        while (Boolean.TRUE.equals(waitingGenerateMap.get(chapterId))) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "요청 처리 중 인터럽트가 발생하였습니다.";
            }
        }
        waitingGenerateMap.put(chapterId, true);
        String content = service.getChapterContentById(chapterId);
        waitingGenerateMap.remove(chapterId);
        return content;
    }
    @MutationMapping
    public LocalDate updateCompleteDate(@Argument int chapterId){
        LocalDate completeDate = service.updateCompleteDate(chapterId);
        return completeDate;
    }
    @QueryMapping
    public List<Chap> getAllChapters(){
        String userId = (String) session.getAttribute("user");
//        String userId="account";
        List<Chap> result = service.getAllChapterByUserId(userId);
        return result;
    }

}
