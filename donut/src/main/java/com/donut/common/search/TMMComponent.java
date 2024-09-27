package com.donut.common.search;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class TMMComponent
{
    @Value("${hybrid-search.tmm.text-score.max-value}")
    double bm25MaxValue;

    @Value("${hybrid-search.tmm.text-score.min-value}")
    double bm25MinValue;

    @Value("${hybrid-search.weight.vector-score}")
    public double vectorScoreWeight;

    @Value("${hybrid-search.weight.text-score}")
    public double textScoreWeight;

    @Autowired
    EmbeddingModel embeddingModel;

    public double getTMMVectorScore(double vectorScore)
    {
        return (vectorScore + 1) / 2.0;
    }

    public double getTMMBM25Score(double bm25Score)
    {
        return Math.min((bm25Score - bm25MinValue) / (bm25MaxValue - bm25MinValue)
                , 1.0);
    }

    public double getConvexScore(double vectorScore, double bm25Score)
    {
        return vectorScoreWeight * getTMMVectorScore(vectorScore)
                + getTMMBM25Score(bm25Score) * textScoreWeight;
    }

    public double getVectorScore(String query, String content)
    {
        List<Double> list1 = embeddingModel.embed(query);
        List<Double> list2 = embeddingModel.embed(content);

        return cosineSimilarity(list1, list2);
    }

    protected double cosineSimilarity(List<Double> vectorA, List<Double> vectorB)
    {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++)
        {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}