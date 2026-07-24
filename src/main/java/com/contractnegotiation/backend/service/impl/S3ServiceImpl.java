package com.contractnegotiation.backend.service.impl;

import com.contractnegotiation.backend.exception.S3DownloadException;
import com.contractnegotiation.backend.exception.S3UploadException;
import com.contractnegotiation.backend.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Service
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final String bucketName;
    private final String region;

    public S3ServiceImpl(
            S3Client s3Client,
            @Value("${aws.s3.bucket-name:contract-negotiation-bucket}") String bucketName,
            @Value("${aws.region:us-east-1}") String region) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.region = region;
    }

    @Override
    public String uploadFile(MultipartFile file, String keyName) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, keyName);
        } catch (S3Exception e) {
            throw new S3UploadException("AWS S3 Error during file upload: " + e.awsErrorDetails().errorMessage(), e);
        } catch (IOException e) {
            throw new S3UploadException("Failed to read file input stream for upload: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new S3UploadException("Unexpected error during S3 file upload: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadFile(String keyOrUrl) {
        try {
            String key = extractKeyFromUrl(keyOrUrl);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            return objectBytes.asByteArray();
        } catch (S3Exception e) {
            throw new S3DownloadException("AWS S3 Error downloading file (" + keyOrUrl + "): " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            throw new S3DownloadException("S3 download failure for (" + keyOrUrl + "): " + e.getMessage(), e);
        }
    }

    private String extractKeyFromUrl(String keyOrUrl) {
        if (keyOrUrl == null) return "";
        if (keyOrUrl.contains(".amazonaws.com/")) {
            return keyOrUrl.substring(keyOrUrl.indexOf(".amazonaws.com/") + 15);
        }
        return keyOrUrl;
    }

    @Override
    public void deleteFile(String keyName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            throw new S3UploadException("AWS S3 Error during file deletion: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            throw new S3UploadException("Unexpected error during S3 file deletion: " + e.getMessage(), e);
        }
    }
}
