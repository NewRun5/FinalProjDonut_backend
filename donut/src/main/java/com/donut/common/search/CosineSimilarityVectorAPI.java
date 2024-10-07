package com.donut.common.search;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CosineSimilarityVectorAPI {
    public static double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Vectors must be of same length");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        // Calculate dot product and norms
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }

        // Handle zero norm cases to avoid division by zero
        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        // Cosine similarity
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
