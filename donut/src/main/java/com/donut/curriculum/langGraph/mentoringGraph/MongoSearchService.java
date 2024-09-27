package com.donut.curriculum.langGraph.mentoringGraph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

@Service
public class MongoSearchService
{
    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    TMMComponent tmmComponent;


    @Autowired
    OpenAiEmbeddingModel openAiEmbeddingModel;



    public AggregationResults<Document> vectorSearch(String query)
    {
        List<Double> queryVector = openAiEmbeddingModel.embed(query);

        Document vectorSearchStage = new Document("$vectorSearch",
                new Document("index", "vector_index") // 벡터 인덱스 이름
                        .append("path", "embedding")
                        .append("queryVector", queryVector) // 벡터로 변환된 쿼리
                        .append("numCandidates", 64) // 후보 문서 수
                        .append("limit", 10)); // 후보 문서 수

        Aggregation aggregation = Aggregation.newAggregation(
                context -> vectorSearchStage,
                // $project 단계 추가
                context -> new Document("$project",
                        new Document("metadata", 1)
                                .append("content", 1)
                                .append("media", 1)
                                .append("vectorScore", new Document("$meta", "vectorSearchScore")) // 검색 점수 추가
                                .append("score", new Document("$meta", "vectorSearchScore")) // 검색 점수 추가
                ),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "score")), // 점수에 따라 정렬
                Aggregation.limit(10) // 상위 10개의 결과만 가져옴
        );

        // Aggregation 실행 및 결과 반환
        return mongoTemplate.aggregate(aggregation, "documents", Document.class);
    }

    public AggregationResults<Document> textSearch(String query)
    {
        Document searchStage = new Document("$search",
                new Document("index", "text_index") // 텍스트 인덱스 이름
                        .append("text",
                                new Document("query", query) // 검색어가 포함된 변수
                                        .append("path", List.of("content", "metadata.title", "metadata.author")) // 검색할 필드 목록
                        )
        );

        // MongoDB Aggregation 생성
        Aggregation aggregation = Aggregation.newAggregation(
                context -> searchStage,
                // $project 단계 추가
                context -> new Document("$project",
                        new Document("metadata", 1)
                                .append("content", 1)
                                .append("media", 1)
                                .append("textScore", new Document("$meta", "searchScore")) // 검색 점수 추가
                                .append("score", new Document("$meta", "searchScore")) // 검색 점수 추가
                ),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "score")),
                // 점수에 따라 정렬
                Aggregation.limit(10) // 상위 10개의 결과만 가져옴
        );

        // Aggregation 실행 및 결과 반환
        return mongoTemplate.aggregate(aggregation, "documents", Document.class);
    }

    public List<Map<String, Object>> hybridSearch(String query)
    {
        AggregationResults<Document> result1 =  vectorSearch(query);
        AggregationResults<Document> result2 =  textSearch(query);

        Map<String, Map<String, Object>> combinedResultsMap = new HashMap<>();

        // 벡터 검색 결과 처리
        for (Document doc : result1.getMappedResults())
        {
            String id = doc.getString("_id");
            double vectorScore = doc.getDouble("vectorScore");

            Map<String, Object> resultMap = combinedResultsMap.getOrDefault(id, new HashMap<>(doc));
            resultMap.put("vectorScore", vectorScore);
            resultMap.put("tmmVectorScore", tmmComponent.getTMMVectorScore(vectorScore) );
            resultMap.put("tmmVectorScoreWeight", tmmComponent.getTMMVectorScore(vectorScore) * tmmComponent.vectorScoreWeight);
            resultMap.put("score", tmmComponent.getConvexScore(vectorScore, 0));
            // 초기 score는 vectorScore로 설정
            combinedResultsMap.put(id, resultMap);
        }

        // 텍스트 검색 결과 처리
        for (Document doc : result2.getMappedResults())
        {
            String id = doc.getString("_id");
            double textScore = doc.getDouble("textScore");

            Map<String, Object> resultMap = combinedResultsMap.getOrDefault(id, new HashMap<>(doc));
            double currentScore = (double) resultMap.getOrDefault("score", 0.0);
            double vectorScore = (double) resultMap.getOrDefault("vectorScore", 0.0);
            if(vectorScore == 0)
            {
                vectorScore = tmmComponent.getVectorScore(query, doc.getString("content"));

                resultMap.put("vectorScore", vectorScore);
                resultMap.put("tmmVectorScore", tmmComponent.getTMMVectorScore(vectorScore) );
                resultMap.put("tmmVectorScoreWeight", tmmComponent.getTMMVectorScore(vectorScore) * tmmComponent.vectorScoreWeight);
            }
            resultMap.put("textScore", textScore);
            resultMap.put("tmmTextScore", tmmComponent.getTMMBM25Score(textScore));
            resultMap.put("tmmTextScoreWeight", tmmComponent.getTMMBM25Score(textScore)* tmmComponent.textScoreWeight);
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