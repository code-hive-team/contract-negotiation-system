package com.contractnegotiation.backend.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ContractTest {

    @Test
    void gettersAndSetters_roundTripAllFields() {
        Contract contract = new Contract();
        User uploader = new User();
        uploader.setId(1L);
        LocalDateTime uploadedAt = LocalDateTime.now();
        byte[] content = "pdf-content".getBytes();

        contract.setId(5L);
        contract.setTitle("Master Services Agreement");
        contract.setOriginalFileName("msa.pdf");
        contract.setFileType("application/pdf");
        contract.setFileSize(1024L);
        contract.setContent(content);
        contract.setS3Url("https://s3/msa.pdf");
        contract.setUploadedBy(uploader);
        contract.setUploadedAt(uploadedAt);
        contract.setStatus(ContractStatus.COMPLETED);

        assertThat(contract.getId()).isEqualTo(5L);
        assertThat(contract.getTitle()).isEqualTo("Master Services Agreement");
        assertThat(contract.getOriginalFileName()).isEqualTo("msa.pdf");
        assertThat(contract.getFileType()).isEqualTo("application/pdf");
        assertThat(contract.getFileSize()).isEqualTo(1024L);
        assertThat(contract.getContent()).isEqualTo(content);
        assertThat(contract.getS3Url()).isEqualTo("https://s3/msa.pdf");
        assertThat(contract.getUploadedBy()).isEqualTo(uploader);
        assertThat(contract.getUploadedAt()).isEqualTo(uploadedAt);
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.COMPLETED);
    }

    @Test
    void onCreate_setsDefaultUploadedAtAndStatus_whenMissing() {
        Contract contract = new Contract();

        contract.onCreate();

        assertThat(contract.getUploadedAt()).isNotNull();
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.UPLOADED);
    }

    @Test
    void onCreate_doesNotOverwriteExistingUploadedAtOrStatus() {
        Contract contract = new Contract();
        LocalDateTime original = LocalDateTime.now().minusDays(2);
        contract.setUploadedAt(original);
        contract.setStatus(ContractStatus.ANALYZING);

        contract.onCreate();

        assertThat(contract.getUploadedAt()).isEqualTo(original);
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.ANALYZING);
    }
}
