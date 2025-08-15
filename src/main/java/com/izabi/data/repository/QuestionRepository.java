package com.izabi.data.repository;

import com.izabi.data.model.Question;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {
    List<Question> findByFileId(String fileId);
    void deleteByFileId(String fileId);
}
