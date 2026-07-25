package com.contractnegotiation.backend.service.impl;

import com.contractnegotiation.backend.dto.ContractDto;
import com.contractnegotiation.backend.dto.FileUploadResponseDto;
import com.contractnegotiation.backend.entity.Contract;
import com.contractnegotiation.backend.entity.ContractStatus;
import com.contractnegotiation.backend.entity.User;
import com.contractnegotiation.backend.exception.ContractFileReadException;
import com.contractnegotiation.backend.exception.InvalidFileException;
import com.contractnegotiation.backend.exception.ResourceNotFoundException;
import com.contractnegotiation.backend.repository.ContractRepository;
import com.contractnegotiation.backend.repository.UserRepository;
import com.contractnegotiation.backend.service.ContractService;
import com.contractnegotiation.backend.service.S3Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public ContractServiceImpl(
            ContractRepository contractRepository,
            UserRepository userRepository,
            S3Service s3Service) {

        this.contractRepository = contractRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
    }

    @Override
    @Transactional
    public FileUploadResponseDto uploadContract(
            MultipartFile file,
            String title,
            String username) {

        validatePdfFile(file);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username));

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "contract.pdf";
        }

        String contractTitle = (title != null && !title.isBlank())
                ? title
                : originalFilename;

        String s3Key = "contracts/" + UUID.randomUUID() + "_" + originalFilename;

        String s3Url = s3Service.uploadFile(file, s3Key);

        Contract contract = new Contract();
        contract.setTitle(contractTitle);
        contract.setOriginalFileName(originalFilename);
        contract.setFileType(
                file.getContentType() != null
                        ? file.getContentType()
                        : "application/pdf");
        contract.setFileSize(file.getSize());

        try {
            contract.setContent(file.getBytes());
        } catch (IOException e) {
            throw new ContractFileReadException(
                    "Failed to read uploaded PDF file.",
                    e
            );
        }

        contract.setS3Url(s3Url);
        contract.setUploadedBy(user);
        contract.setStatus(ContractStatus.UPLOADED);

        Contract savedContract = contractRepository.save(contract);
        FileUploadResponseDto response = new FileUploadResponseDto();

        response.setMessage("Contract PDF uploaded successfully");
        response.setContractId(savedContract.getId());
        response.setTitle(savedContract.getTitle());
        response.setFileName(savedContract.getOriginalFileName());
        response.setFileType(savedContract.getFileType());
        response.setFileSize(savedContract.getFileSize());
        response.setS3Url(savedContract.getS3Url());
        response.setStatus(savedContract.getStatus());

        return response;
       
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDto getContractById(Long id) {

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Contract not found with id: " + id));

        return mapToDto(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDto> getContractsByUserId(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + userId);
        }

        return contractRepository.findByUploadedById(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDto> getAllContracts() {

        return contractRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private void validatePdfFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidFileException(
                    "File is required and cannot be empty");
        }

        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();

        boolean isPdfExtension =
                filename != null &&
                        filename.toLowerCase().endsWith(".pdf");

        boolean isPdfContentType =
                contentType != null &&
                        contentType.equalsIgnoreCase("application/pdf");

        if (!isPdfExtension && !isPdfContentType) {
            throw new InvalidFileException(
                    "Only PDF files are allowed.");
        }
    }

    private ContractDto mapToDto(Contract contract) {

        ContractDto dto = new ContractDto();

        dto.setId(contract.getId());
        dto.setTitle(contract.getTitle());
        dto.setOriginalFileName(contract.getOriginalFileName());
        dto.setFileType(contract.getFileType());
        dto.setFileSize(contract.getFileSize());
        dto.setS3Url(contract.getS3Url());

        if (contract.getUploadedBy() != null) {
            dto.setUploadedById(contract.getUploadedBy().getId());
            dto.setUploadedByUsername(contract.getUploadedBy().getUsername());
        }

        dto.setUploadedAt(contract.getUploadedAt());
        dto.setStatus(contract.getStatus());

        return dto;
    }
}