package com.izabi.mapper;

import com.izabi.data.model.Embedding;
import com.izabi.dto.response.EmbeddingResponse;

public class EmbeddingMapper {
    public static EmbeddingResponse mapToEmbeddingResponse(String message, Embedding embedding) {
        EmbeddingResponse embeddingResponse = new EmbeddingResponse();
        embeddingResponse.setMessage(message);
        embeddingResponse.setEmbedding(embedding);
        return embeddingResponse;
    }
}
