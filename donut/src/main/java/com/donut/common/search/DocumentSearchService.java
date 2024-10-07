package com.donut.common.search;

import lombok.RequiredArgsConstructor;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentSearchService {
    private final MongoTemplate mongoTemplate;
    private final TMMComponent tmmComponent;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;

    public AggregationResults<Document> vectorSearch(String query) {
        List<Double> queryVector = openAiEmbeddingModel.embed(query);

        Document vectorSearchStage = new Document("$vectorSearch",
                new Document("index", "vector-index") // 벡터 인덱스 이름
                        .append("path", "content-embedding")
                        .append("queryVector", queryVector) // 벡터로 변환된 쿼리
                        .append("numCandidates", 64) // 후보 문서 수
                        .append("limit", 30) // 후보 문서 수
                        .append("filter", new Document("meta.type", "doc"))
        );

        Aggregation aggregation = Aggregation.newAggregation(
                context -> vectorSearchStage,
                context -> new Document("$project",
                        new Document("meta", 1)
                                .append("vectorScore", new Document("$meta", "vectorSearchScore")) // 검색 점수 추가
                ),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "vectorScore")), // 점수에 따라 정렬
                Aggregation.limit(30) // 상위 10개의 결과만 가져옴
        );

        // Aggregation 실행 및 결과 반환
        return mongoTemplate.aggregate(aggregation, "document", Document.class);
    }
    public AggregationResults<Document> textSearch(String query) {
        Document searchStage = new Document("$search",
                new Document("index", "text-index") // 텍스트 인덱스 이름
                        .append("text", new Document("query", query)
                                .append("path", List.of("meta.content", "meta.title"))
                        )
        );
        Document matchStage = new Document("$match", new Document("meta.type", "doc"));

        Aggregation aggregation = Aggregation.newAggregation(
                context -> searchStage,
                context -> matchStage,
                context -> new Document("$project",
                        new Document("meta", 1)
                                .append("textScore", new Document("$meta", "searchScore")) // 검색 점수 추가
                ),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "textScore")),
                Aggregation.limit(30) // 상위 10개의 결과만 가져옴
        );

        // Aggregation 실행 및 결과 반환
        return mongoTemplate.aggregate(aggregation, "document", Document.class);
    }

    public List<Map<String, Object>> hybridSearch(String query) {
        AggregationResults<Document> result1 = vectorSearch(query);
        AggregationResults<Document> result2 = textSearch(query);

        Map<String, Map<String, Object>> combinedResultsMap = new HashMap<>();

        // 벡터 검색 결과 처리
        for (Document doc : result1.getMappedResults())
        {
            ObjectId objectId = doc.getObjectId("_id"); // _id를 ObjectId로 가져옴
            String id = objectId.toHexString(); // ObjectId를 String으로 변환
            double vectorScore = doc.getDouble("vectorScore");
            Map<String, Object> resultMap = combinedResultsMap.getOrDefault(id, new HashMap<>(doc));
            resultMap.put("score", tmmComponent.getConvexScore(vectorScore, 0));
            // 초기 score는 vectorScore로 설정
            combinedResultsMap.put(id, resultMap);
        }

        // 텍스트 검색 결과 처리
        for (Document doc : result2.getMappedResults())
        {
            ObjectId objectId = doc.getObjectId("_id"); // _id를 ObjectId로 가져옴
            String id = objectId.toHexString(); // ObjectId를 String으로 변환
            double textScore = doc.getDouble("textScore");

            Map<String, Object> resultMap = combinedResultsMap.getOrDefault(id, new HashMap<>(doc));
            double currentScore = (double) resultMap.getOrDefault("score", 0.0);
            double vectorScore = (double) resultMap.getOrDefault("vectorScore", 0.0);
            resultMap.put("score", tmmComponent.getConvexScore(vectorScore, textScore)); // 기존 score에 searchScore를 더함
            combinedResultsMap.put(id, resultMap);
        }

        // 결과를 리스트로 변환 및 정렬
        List<Map<String, Object>> combinedResults = new ArrayList<>(combinedResultsMap.values());

        if(combinedResults != null && combinedResults.size() > 1)
        {
            combinedResults.sort((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")));
        }
        return combinedResults;
    }
}
