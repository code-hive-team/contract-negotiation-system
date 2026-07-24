package com.contractnegotiation.backend.dto;

import com.contractnegotiation.backend.entity.ContractStatus;

public class FileUploadResponseDto {
    private String message;
    private Long contractId;
    private String title;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String s3Url;
    private ContractStatus status;

    public FileUploadResponseDto() {
    }

    public FileUploadResponseDto(String message, Long contractId, String title, String fileName, String fileType, Long fileSize, String s3Url, ContractStatus status) {
        this.message = message;
        this.contractId = contractId;
        this.title = title;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.s3Url = s3Url;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }
}
