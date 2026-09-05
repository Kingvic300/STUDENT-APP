package com.izabi.data.repository;

import com.izabi.data.model.Embedding;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmbeddingRepository extends MongoRepository<Embedding , String> {
}
