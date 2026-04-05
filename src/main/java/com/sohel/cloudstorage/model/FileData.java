package com.sohel.cloudstorage.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class FileData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String path;
    private String type;

    public FileData() {}

    public FileData(String name, String path, String type) {
        this.name = name;
        this.path = path;
        this.type = type;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPath() { return path; }
    public String getType() { return type; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPath(String path) { this.path = path; }
    public void setType(String type) { this.type = type; }
}