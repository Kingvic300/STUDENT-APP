package com.izabi.data.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Document(collection = "embeddings")
public class Embedding {

    @Id
    private String id;

    @Field("voice_print")
    private String voicePrint;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("feature_count")
    private int Feature_count;

    public List<Double> getVoicePrintList() {
        if (voicePrint == null || voicePrint.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(voicePrint.split(","))
                .map(String::trim)
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }

    public void setVoicePrintList(List<Double> list) {
        if (list == null || list.isEmpty()) {
            this.voicePrint = "";
        } else {
            this.voicePrint = list.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
    }

    @Field("voice_print_array")
    private List<Double> voicePrintArray;

    public Embedding() {
        this.createdAt = LocalDateTime.now();
    }
}