package com.donut.chapter.questionChatBot.langGraph;

import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;
import java.util.Optional;
import java.util.List;

/**
 * QuestionState 클래스는 AgentState를 상속하여
 * LangGraph에서 질문과 관련된 상태 데이터를 저장하고 관리하는 역할을 합니다.
 */
public class QuestionState extends AgentState {

    /**
     * 초기 상태 데이터를 기반으로 QuestionState를 생성하는 생성자.
     * @param initData 초기 상태 데이터를 담고 있는 Map.
     */
    public QuestionState(Map<String, Object> initData) {
        super(initData);  // 부모 클래스인 AgentState의 생성자를 호출하여 초기 데이터 설정
    }

    /**
     * 상태에 저장된 질문 데이터를 반환합니다.
     * @return 질문 문자열.
     * @throws IllegalStateException 질문 데이터가 설정되지 않았을 경우 예외 발생.
     */
    public String question() {
        Optional<String> result = value("question");  // "question" 키로 저장된 값을 Optional로 가져옴
        return result.orElseThrow(() -> new IllegalStateException("question is not set!"));  // 값이 없으면 예외 발생
    }

    /**
     * 상태에 저장된 검색 쿼리 데이터를 반환합니다.
     * @return 검색 쿼리 문자열.
     * @throws IllegalStateException 검색 쿼리가 설정되지 않았을 경우 예외 발생.
     */
    public String search_query() {
        return this.<String>value("search_query").orElseThrow(() ->  // "search_query" 키로 값을 가져옴
                new IllegalStateException("search_query is not set!"));  // 값이 없으면 예외 발생
    }

    /**
     * 상태에 저장된 검색 시도 횟수를 반환합니다.
     * @return 검색 시도 횟수 (없을 경우 기본값은 0).
     */
    public int search_count() {
        return this.<Integer>value("search_count").orElse(0);  // "search_count" 키로 값을 가져옴 (없을 경우 0 반환)
    }

    /**
     * 상태에 저장된 검색 결과 문서 목록을 반환합니다.
     * @return 검색된 문서의 리스트 (없을 경우 빈 리스트를 반환).
     */
    public List<Map<String, Object>> documents() {
        return this.<List<Map<String, Object>>>value("documents").orElse(List.of());  // "documents" 키로 값을 가져옴 (없을 경우 빈 리스트 반환)
    }
}
