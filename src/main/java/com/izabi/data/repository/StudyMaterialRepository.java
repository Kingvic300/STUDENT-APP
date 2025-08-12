package com.izabi.data.repository;

import com.izabi.data.model.StudyMaterial;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyMaterialRepository extends MongoRepository<StudyMaterial, String> {
}
