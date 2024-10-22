package com.donut.curriculum;

import com.donut.common.gson.JsonUtil;
import com.donut.common.utils.ChatBotComponent;
import com.donut.common.utils.FileUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.image.ImageResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CurriculumService {
    private final CurriculumMapper mapper;
    private final ChatBotComponent component;
    private final JsonUtil jsonUtil;
    private final FileUtil fileUtil;
    private final HttpSession session;
    @Transactional
    public Integer saveCurriculum(CurriculumDTO request) {
        // 커리큘럼 생성 날짜 설정

        request.setCreateDate(LocalDate.now());
        request.setUserId((String) session.getAttribute("user"));
        request.setProgress((float) 0);
        // 커리큘럼 저장
        mapper.saveCurriculum(request);

        // 챕터 리스트가 있을 경우 챕터 저장
        if (request.getChapterList() != null && !request.getChapterList().isEmpty()) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("curriculumId", request.getId());
            paramMap.put("chapters", request.getChapterList());

            // 챕터 리스트를 한 번에 저장
            mapper.saveChapters(paramMap);
        }

        // 저장된 커리큘럼의 id 반환
        return request.getId();
    }

    @Transactional
    public CurriculumDTO getCurriculumById(Integer id) {
        CurriculumDTO result = mapper.getCurriculumById(id);
//        if(result.getImagePath() == null){
//            String imagePath = generateImage(result);
//            result.setImagePath(imagePath);
//        }
        return result;
    }
    private String generateImage(CurriculumDTO curriculum) {
        System.out.println("이미지 생성 시작");
        String prompt = "context 와 어울리는 심플한 학습용 이미지 생성. 글씨는 사용하면 안됨. \n" +
                "이미지는 세로로 생성."+
                "### context ### \n" +
                "title : " + curriculum.getTitle() + "\n" +
                "description : " + curriculum.getDescription();
        String imagePath = component.getImageByString(prompt);
        String result = null;
        try {
            result = fileUtil.saveImage(imagePath);
            System.out.println("저장완료" + result);
        } catch (Exception e) {
            System.out.println("오류발생");
            e.printStackTrace();
        }
        if (result != null){
            int saveResult = mapper.saveImagePath(Map.of("imagePath", result, "curriculumId", curriculum.getId()+""));
            System.out.println("커리큘럼 이미지 경로 저장 완료");
        }
        return result;
    }

    public List<CurriculumDTO> getCurriculumListByUserId(String userId) {
        return mapper.getCurriculumListByUserId(userId);
    }
}
