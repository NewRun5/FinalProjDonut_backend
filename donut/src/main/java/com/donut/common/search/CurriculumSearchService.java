package com.donut.common.search;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
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
public class CurriculumSearchService {
    private final MongoTemplate mongoTemplate;
    private final TMMComponent tmmComponent;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;

    public AggregationResults<Document> vectorSearch(String query) {
        List<Double> queryVector = openAiEmbeddingModel.embed(query);

        Document vectorSearchStage = new Document("$vectorSearch",
                new Document("index", "vector-index") // 벡터 인덱스 이름
                        .append("path", "content-embedding")
                        .append("queryVector", queryVector) // 벡터로 변환된 쿼리
                        .append("numCandidates", 30) // 후보 문서 수
                        .append("limit", 10) // 후보 문서 수
                        .append("filter", new Document("meta.type", "curriculum"))
        );

        Aggregation aggregation = Aggregation.newAggregation(
                context -> vectorSearchStage,
                context -> new Document("$project",
                        new Document("meta", 1)
                                .append("vectorScore", new Document("$meta", "vectorSearchScore")) // 검색 점수 추가
                ),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "vectorScore")), // 점수에 따라 정렬
                Aggregation.limit(10) // 상위 10개의 결과만 가져옴
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
        Document matchStage = new Document("$match", new Document("meta.type", "curriculum"));

        Aggregation aggregation = Aggregation.newAggregation(
                context -> searchStage,
                context -> matchStage,
                context -> new Document("$project",
                        new Document("meta", 1)
                                .append("textScore", new Document("$meta", "searchScore")) // 검색 점수 추가
                ),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "textScore")),
                Aggregation.limit(10) // 상위 10개의 결과만 가져옴
        );

        // Aggregation 실행 및 결과 반환
        return mongoTemplate.aggregate(aggregation, "document", Document.class);
    }

    public List<Map<String, Object>> hybridSearch(String query) {
        List<Double> queryVector = openAiEmbeddingModel.embed(query); // 쿼리 벡터 생성
        AggregationResults<Document> vectorSearchResults = vectorSearch(query);
        AggregationResults<Document> textSearchResults = textSearch(query);

        Map<String, Map<String, Object>> combinedResultsMap = new HashMap<>();

        // 벡터 검색 결과 처리
        List<ObjectId> missingTextIds = new ArrayList<>(); // 텍스트 검색에 누락된 벡터 _id 리스트
        for (Document doc : vectorSearchResults.getMappedResults()) {
            ObjectId objectId = doc.getObjectId("_id");
            String id = objectId.toHexString();
            double vectorScore = doc.getDouble("vectorScore");

            Map<String, Object> resultMap = combinedResultsMap.getOrDefault(id, new HashMap<>(doc));
            resultMap.put("score", tmmComponent.getConvexScore(vectorScore, 0)); // 초기 score는 vectorScore로 설정
            combinedResultsMap.put(id, resultMap);

            // 나중에 텍스트 검색에 추가할 ID 저장
            missingTextIds.add(objectId);
        }

        List<ObjectId> missingVectorIds = new ArrayList<>();

        // 텍스트 검색 결과 처리
        for (Document doc : textSearchResults.getMappedResults()) {
            ObjectId objectId = doc.getObjectId("_id");
            String id = objectId.toHexString();
            double textScore = doc.getDouble("textScore");

            Map<String, Object> resultMap = combinedResultsMap.getOrDefault(id, new HashMap<>(doc));

            // 벡터 스코어가 없는 경우, id를 수집해서 나중에 벡터 검색에 사용
            if (!resultMap.containsKey("vectorScore")) {
                missingVectorIds.add(objectId); // _id 추가
            } else {
                double vectorScore = (double) resultMap.getOrDefault("vectorScore", 0.0);
                resultMap.put("score", tmmComponent.getConvexScore(vectorScore, textScore)); // 기존 score에 textScore를 더함
            }

            combinedResultsMap.put(id, resultMap);

            // 텍스트 검색에 성공한 _id는 missingTextIds에서 제거
            missingTextIds.remove(objectId);
        }

        // 벡터 스코어가 없는 문서들에 대해 추가로 벡터 검색 수행
        if (!missingVectorIds.isEmpty()) {
            System.out.println("텍스트 스코어만 있는 문서들의 _id: " + missingVectorIds);

            AggregationResults<Document> missingVectorSearchResults = vectorSearchByIds(missingVectorIds, queryVector); // 쿼리 벡터 전달

            for (Document doc : missingVectorSearchResults.getMappedResults()) {
                ObjectId objectId = doc.getObjectId("_id");
                String id = objectId.toHexString();
                double vectorScore = doc.getDouble("vectorScore");

                // 해당 문서의 텍스트 스코어를 이미 계산했기 때문에 score를 업데이트
                Map<String, Object> resultMap = combinedResultsMap.get(id);
                double textScore = (double) resultMap.getOrDefault("textScore", 0.0);
                resultMap.put("score", tmmComponent.getConvexScore(vectorScore, textScore)); // 벡터와 텍스트 점수를 결합
                resultMap.put("vectorScore", vectorScore); // 벡터 스코어 추가
            }
        }

        // 텍스트 검색이 누락된 벡터 스코어가 있는 문서들에 대해 추가 텍스트 검색 수행
        if (!missingTextIds.isEmpty()) {
            System.out.println("벡터 스코어만 있는 문서들의 _id: " + missingTextIds);

            AggregationResults<Document> missingTextSearchResults = textSearchByIds(missingTextIds, query);

            for (Document doc : missingTextSearchResults.getMappedResults()) {
                ObjectId objectId = doc.getObjectId("_id");
                String id = objectId.toHexString();
                double textScore = doc.getDouble("textScore");

                // 해당 문서의 벡터 스코어를 이미 계산했기 때문에 score를 업데이트
                Map<String, Object> resultMap = combinedResultsMap.get(id);
                double vectorScore = (double) resultMap.getOrDefault("vectorScore", 0.0);
                resultMap.put("score", tmmComponent.getConvexScore(vectorScore, textScore)); // 벡터와 텍스트 점수를 결합
                resultMap.put("textScore", textScore); // 텍스트 스코어 추가
            }
        }

        // 결과를 리스트로 변환 및 정렬
        List<Map<String, Object>> combinedResults = new ArrayList<>(combinedResultsMap.values());

        if (combinedResults != null && combinedResults.size() > 1) {
            combinedResults.sort((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")));
        }

        return combinedResults;
    }


    private AggregationResults<Document> vectorSearchByIds(List<ObjectId> idList, List<Double> queryVector) {
        Document vectorSearchStage = new Document("$vectorSearch",
                new Document("index", "vector-index")
                        .append("path", "content-embedding")
                        .append("queryVector", queryVector) // 올바른 쿼리 벡터 전달
                        .append("numCandidates", 64)
                        .append("limit", 30)
                        .append("filter", new Document("_id", new Document("$in", idList)))
        );

        Aggregation aggregation = Aggregation.newAggregation(
                context -> vectorSearchStage,
                context -> new Document("$project",
                        new Document("meta", 1)
                                .append("vectorScore", new Document("$meta", "vectorSearchScore"))
                )
        );

        return mongoTemplate.aggregate(aggregation, "document", Document.class);
    }
    private AggregationResults<Document> textSearchByIds(List<ObjectId> idList, String query) {
        Document searchStage = new Document("$search",
                new Document("index", "text-index")
                        .append("text", new Document("query", query)
                                .append("path", List.of("meta.content", "meta.title"))
                        )
        );

        Document matchStage = new Document("$match", new Document("_id", new Document("$in", idList)));

        Aggregation aggregation = Aggregation.newAggregation(
                context -> searchStage,
                context -> matchStage,
                context -> new Document("$project",
                        new Document("meta", 1)
                                .append("textScore", new Document("$meta", "searchScore"))
                )
        );

        return mongoTemplate.aggregate(aggregation, "document", Document.class);
    }
}
