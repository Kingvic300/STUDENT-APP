package com.izabi.data.repository;

import com.izabi.data.model.StudyQuestion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyQuestionRepository extends MongoRepository<StudyQuestion, String> {
    List<StudyQuestion> findByStudyMaterialIdAndActive(String pdfId, boolean b);
}
