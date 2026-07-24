package com.contractnegotiation.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    String uploadFile(MultipartFile file, String keyName);

    byte[] downloadFile(String keyOrUrl);

    void deleteFile(String keyName);
}
