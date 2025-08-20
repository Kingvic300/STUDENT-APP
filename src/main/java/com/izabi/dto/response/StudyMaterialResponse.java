package com.izabi.dto.response;

import com.izabi.data.model.StudyQuestion;
import lombok.Data;

import java.util.List;

@Data
public class StudyMaterialResponse {
    private String summary;
    private List<String> keypoint;
    private List<StudyQuestion> questions;
    private String message;

}
