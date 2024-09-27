package com.donut.curriculum.langGraph.mentoringGraph;

import com.donut.common.utils.ChatBotComponent;
import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@Service
@RequiredArgsConstructor
public class MRS {
    public class TestState extends AgentState
    {
        public TestState(Map<String, Object> initData)
        {
            super(initData);
            // TODO Auto-generated constructor stub
        }


        /*
         * 유저의 질의는 question으로 받음
         * https://github.com/bsorrentino/langgraph4j/blob/main/adaptive-rag/src/main/java/dev/langchain4j/adaptiverag/AdaptiveRag.java
         *
         * graph.stream( mapOf( "question", "What player at the Bears expected to draft first in the 2024 NFL draft?" ) );
         */
        public String question()
        {
            return this.<String>value("question").orElseThrow(() ->
                    new IllegalStateException("question is not set!"));
        }

        public String search_query()
        {
            return this.<String>value("search_query").orElseThrow(() ->
                    new IllegalStateException("search_query is not set!"));
        }
        public String search_type()
        {
            // web_search, rag_search
            return this.<String>value("search_type").orElse("web_search");
        }
        public int search_count()
        {
            return this.<Integer>value("search_count").orElse(0);
        }
        public List<Map<String, Object>> documents()
        {
            return this.<List<Map<String, Object>>>value("documents").orElse(List.of());
        }
        public Optional<String> generation()
        {
            return value("generation");
        }
    }
    private final ChatBotComponent component;

    private final MongoSearchService mongoService;

    private final Gson gson;

    private final double THRESHOLD = 0.01;

    CompiledGraph<TestState> app = null;

    @PostConstruct
    public void init() throws Exception {
        app = this.buildGraph();
    }

    public CompiledGraph<TestState> getApp() {
        return app;
    }

    public CompiledGraph<TestState> buildGraph() throws GraphStateException {
        return new StateGraph<>(TestState::new)

                .addNode("web_query", node_async(this::web_query))
                .addNode("web_search", node_async(this::web_search))

                .addNode("rag_query", node_async(this::rag_query))
                .addNode("rag_search", node_async(this::rag_search))

                .addNode("condition_3_node", node_async(this::condition_3_node))
                .addNode("simple_ganeration", node_async(this::simple_generation))


                .addConditionalEdges(START, edge_async(this::condition_1)
                        , mapOf(
                                "simple_answer", "simple_ganeration"
                                , "web_search", "rag_query"
                                , "rag_search", "rag_query"
                        ))

                .addConditionalEdges("web_search", edge_async(this::condition_2)
                        , mapOf(
                                "simple_answer", "simple_ganeration"
                                , "web_search", "web_query"
                                , "rag_search", "rag_query"
                                , "condition_3", "condition_3_node"
                        ))
                .addConditionalEdges("rag_search", edge_async(this::condition_2)
                        , mapOf(
                                "simple_answer", "simple_ganeration"
                                , "web_search", "web_query"
                                , "rag_search", "rag_query"
                                , "condition_3", "condition_3_node"
                        ))

                .addConditionalEdges("condition_3_node", edge_async(this::condition_3)
                        , mapOf(
                                "simple_answer", "simple_ganeration"
                                , "complicate_document", "simple_ganeration"
                        ))

                .addEdge("rag_query", "rag_search")
                .addEdge("web_query", "web_search")

                .addEdge("simple_ganeration", END)
                .compile();
    }


    private String condition_1(TestState state) {

        String question = state.question();

        String prompt =
                "You are the function below. Respond only with one of the following string values: \r\n" +
                        "simple_answer, rag_search, or web_search\r\n" +
                        "Do not generate code or additional explanations.\r\n" +
                        "\r\n" +
                        "### Function ###\r\n" +
                        "public String checkToNeedRAGSearch(String question)  \r\n" +
                        "{  \r\n" +
                        "    /*  \r\n" +
                        "    Determine the method required to answer the question based on the context.\r\n" +
                        "    \r\n" +
                        "    Return `simple_answer` if the agent can generate a response without needing RAG or external search.\r\n" +
                        "    Return `rag_search` if the agent requires internal RAG search (especially for topics related to internal documents such as game updates, planning, patches, or design changes).\r\n" +
                        "    Return `web_search` if the agent requires an external web search to find the context for answering.\r\n" +
                        "    */\r\n" +
                        "    \r\n" +
                        "    String result;\r\n" +
                        "    \r\n" +
                        "    // Logic to determine the result based on question and answer goes here\r\n" +
                        "\r\n" +
                        "    return result;\r\n" +
                        "}" +
                        "\r\n" +
                        "### question ###\r\n";

        String generation = component.getChatResponse(prompt + question).getContent();

        String result = "simple_answer";
        if (generation.contains("rag_search")) {
            result = "rag_search";
        } else if (generation.contains("web_search")) {
            result = "web_search";
        }

        return result;
    }

    private String condition_2(TestState state) {
        List<Map<String, Object>> documents = state.documents();
        String search_type = state.search_type();
        int search_count = state.search_count();

        // 검색 결과가 없는 경우
        if (documents.size() == 0) {
            return search_type;
        }

        if (!documents.get(0).containsKey("score") || search_count >= 3) {
            return "simple_answer";
        }

        // 최고점 항목이 Threshold를 넘은 경우
        if ((double) documents.get(0).get("score") >= THRESHOLD) {
            return "condition_3";
        }

        return search_type;
    }

    private Map<String, Object> condition_3_node(TestState state) {
        return mapOf();
    }

    private String condition_3(TestState state) {
        String question = state.question();

        String prompt =
                "You are the function provided below. Respond only with one of the following string values:\r\n" +
                        "simple_answer, complicate_document\r\n" +
                        "Do not generate code or additional explanations.\r\n" +
                        "### Function ###\r\n" +
                        "public String checkToUserRequest(String question) { \r\n" +
                        "    /*\r\n" +
                        "    Determine the appropriate method for answering the user's question based on the context.\r\n" +
                        "    \r\n" +
                        "    - Return `simple_answer` if the user is asking for a straightforward response from the AI agent.\r\n" +
                        "    - Return `complicate_document` if the user is requesting a complex task, such as generating a detailed document or report (e.g., company report, translations, etc.).\r\n" +
                        "    */\r\n" +
                        "    \r\n" +
                        "    String result;\r\n" +
                        "    \r\n" +
                        "    // Logic to determine the result based on the question goes here\r\n" +
                        "    \r\n" +
                        "    return result;\r\n" +
                        "}\r\n" +
                        "### question ###";

        String generation = component.getChatResponse(prompt + question).getContent();

        String result = "simple_answer";
        if (generation.contains("complicate_document")) {
            result = "complicate_document";
        }
        return result;
    }


    private Map<String, Object> web_query(TestState state) {
        String question = state.question();

        String prompt =
                "You are the function below. Respond only with the search_query value.\r\n" +
                        "\r\n" +
                        "### Function ###\r\n" +
                        "public String makeSearchQueryFromQuestion(String question) \r\n" +
                        "{ \r\n" +
                        "    /* \r\n" +
                        "    Analyze the given question to identify the relevant keywords and determine the search strategy.\r\n" +
                        "\r\n" +
                        "    generate original language search query `origin_lang_query` first from the question. \r\n" +
                        "    translate `origin_lang_query` to English as `english_query`\r\n" +
                        "    Construct a search query using the extracted `origin_lang_query` and `english_query`.\r\n" +
                        "    \r\n" +
                        "    Return the search_query formatted as: `origin_lang_query` `english_query` \r\n" +
                        "\r\n" +
                        "    */\r\n" +
                        "\r\n" +
                        "    String search_query = origin_lang_query+ \" \" + english_query;\r\n" +
                        "    \r\n" +
                        "    return search_query;\r\n" +
                        "}\r\n" +
                        "\r\n" +
                        "### question ###\r\n";

        String generation = component.getChatResponse(prompt + question).getContent();
        return mapOf("search_query", generation, "search_type", "web_search");
    }

    private Map<String, Object> web_search(TestState state) {
        String search_query = state.search_query();
        int search_count = state.search_count();
        search_count++;
        List<Map<String, Object>> documents = mongoService.hybridSearch(search_query);


        return mapOf("documents", documents, "search_count", search_count);
    }

    private Map<String, Object> rag_query(TestState state) {
        String question = state.question();

        String prompt =
                "You are the function below. Respond only with the search_query value.\r\n" +
                        "\r\n" +
                        "### Function ###\r\n" +
                        "public String makeSearchQueryFromQuestion(String question) \r\n" +
                        "{ \r\n" +
                        "    /* \r\n" +
                        "    Analyze the given question to identify the relevant keywords and determine the search strategy.\r\n" +
                        "\r\n" +
                        "    generate original language search query `origin_lang_query` first from the question. \r\n" +
                        "    translate `origin_lang_query` to English as `english_query`\r\n" +
                        "    Construct a search query using the extracted `origin_lang_query` and `english_query`.\r\n" +
                        "    \r\n" +
                        "    Return the search_query formatted as: `origin_lang_query` `english_query` \r\n" +
                        "\r\n" +
                        "    */\r\n" +
                        "\r\n" +
                        "    String search_query = origin_lang_query+ \" \" + english_query;\r\n" +
                        "    \r\n" +
                        "    return search_query;\r\n" +
                        "}\r\n" +
                        "\r\n" +
                        "### question ###\r\n";

        String generation = component.getChatResponse(prompt + question).getContent();
        return mapOf("search_query", generation, "search_type", "rag_search");
    }

    private Map<String, Object> rag_search(TestState state) {
        String search_query = state.search_query();
        int search_count = state.search_count();
        search_count++;
        List<Map<String, Object>> documents = mongoService.hybridSearch(search_query);
        System.out.println("조회한 문서 수" + documents.size());
        return mapOf("documents", documents, "search_count", search_count);
    }

    private Map<String, Object> simple_generation(TestState state) {
        String question = state.question();
        List<Map<String, Object>> documents = state.documents();

        String strDocuments = gson.toJson(documents);
        System.out.println(documents);
        String generation = component.getChatResponseWithSysMsg("You are an assistant for question-answering tasks\r\n ### context ###\r\n" + strDocuments, question).getContent();

        return mapOf("generation", generation);
    }

}
