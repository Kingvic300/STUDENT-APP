package com.izabi.dto.request;

import com.izabi.dto.response.StudyQuestionResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudyMaterialRequest {
    private String summary;
    private List<String> keyPoints;
    private List<StudyQuestionResponse> questions;

}
