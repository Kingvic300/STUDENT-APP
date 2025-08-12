package com.izabi.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data

public class StudyMaterialDTO {
    private String summary;
    private List<String> keyPoints;
    private List<StudyQuestionDTO> questions;

}
