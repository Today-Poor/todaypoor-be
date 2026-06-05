package com.todaypoor.ai.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.todaypoor.ai.entity.OcrResult;

public interface OcrResultRepository extends JpaRepository<OcrResult, UUID> {
    //일단 임시로 넣은거라 이걸로도 작동
}