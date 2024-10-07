package com.donut.curriculum;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurriculumService {
    private final CurriculumMapper mapper;

    @Transactional
    public Integer saveCurriculum(CurriculumDTO request) {
        // 커리큘럼 생성 날짜 설정
        request.setCreateDate(LocalDate.now());
        request.setUserId("account");
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

    public CurriculumDTO getCurriculumById(Integer id) {
        return mapper.getCurriculumById(id);
    }
}
