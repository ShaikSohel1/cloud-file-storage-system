package com.sohel.cloudstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sohel.cloudstorage.model.FileData;

public interface FileRepository extends JpaRepository<FileData, Long> {
    FileData findByName(String name);
}