package com.izabi.data.repository;

import com.izabi.data.model.Pdf;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PdfRepository extends MongoRepository<Pdf, String> {
}
