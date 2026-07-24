package com.contractnegotiation.backend.dto;

import com.contractnegotiation.backend.entity.ContractStatus;
import java.time.LocalDateTime;

public class ContractDto {
    private Long id;
    private String title;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private String s3Url;
    private Long uploadedById;
    private String uploadedByUsername;
    private LocalDateTime uploadedAt;
    private ContractStatus status;

    public ContractDto() {
    }

    public ContractDto(Long id, String title, String originalFileName, String fileType, Long fileSize, String s3Url, Long uploadedById, String uploadedByUsername, LocalDateTime uploadedAt, ContractStatus status) {
        this.id = id;
        this.title = title;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.s3Url = s3Url;
        this.uploadedById = uploadedById;
        this.uploadedByUsername = uploadedByUsername;
        this.uploadedAt = uploadedAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getS3Url() {
        return s3Url;
    }

    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }

    public Long getUploadedById() {
        return uploadedById;
    }

    public void setUploadedById(Long uploadedById) {
        this.uploadedById = uploadedById;
    }

    public String getUploadedByUsername() {
        return uploadedByUsername;
    }

    public void setUploadedByUsername(String uploadedByUsername) {
        this.uploadedByUsername = uploadedByUsername;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }
}
