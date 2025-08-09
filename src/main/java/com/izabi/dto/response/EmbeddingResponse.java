package com.izabi.dto.response;

import com.izabi.data.model.Embedding;
import lombok.Data;

@Data
public class EmbeddingResponse {
    private String message;
    private Embedding embedding;
}
