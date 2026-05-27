package org.example.storageservice.application.repository;

import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.domain.part.models.Part;

import java.util.List;

public interface PartRepository extends Repository<Part> {

    List<Part> findByType(PartType type);
}