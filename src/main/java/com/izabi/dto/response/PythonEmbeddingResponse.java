package com.izabi.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PythonEmbeddingResponse {
    private String file_id;
    private List<Double> embedding;
    private int feature_count;
}
