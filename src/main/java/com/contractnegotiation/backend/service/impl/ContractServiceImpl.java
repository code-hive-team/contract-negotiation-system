package com.contractnegotiation.backend.service.impl;

import com.contractnegotiation.backend.dto.ContractDto;
import com.contractnegotiation.backend.dto.FileUploadResponseDto;
import com.contractnegotiation.backend.entity.Contract;
import com.contractnegotiation.backend.entity.ContractStatus;
import com.contractnegotiation.backend.entity.User;
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
    public FileUploadResponseDto uploadContract(MultipartFile file, String title, String username) {
        validatePdfFile(file);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            originalFilename = "contract.pdf";
        }

        String contractTitle = (title != null && !title.trim().isEmpty())
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
            throw new RuntimeException("Failed to read uploaded PDF", e);
        }

        contract.setS3Url(s3Url);
        contract.setUploadedBy(user);
        contract.setStatus(ContractStatus.UPLOADED);

        Contract savedContract = contractRepository.save(contract);
        
        return new FileUploadResponseDto(
                "Contract PDF uploaded successfully",
                savedContract.getId(),
                savedContract.getTitle(),
                savedContract.getOriginalFileName(),
                savedContract.getFileType(),
                savedContract.getFileSize(),
                savedContract.getS3Url(),
                savedContract.getStatus()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDto getContractById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));

        return mapToDto(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDto> getContractsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
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
            throw new InvalidFileException("File is required and cannot be empty");
        }

        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();

        boolean isPdfExtension = filename != null && filename.toLowerCase().endsWith(".pdf");
        boolean isPdfContentType = contentType != null && contentType.equalsIgnoreCase("application/pdf");

        if (!isPdfExtension && !isPdfContentType) {
            throw new InvalidFileException("Invalid file format. Only PDF files are allowed.");
        }
    }

    private ContractDto mapToDto(Contract contract) {
        return new ContractDto(
                contract.getId(),
                contract.getTitle(),
                contract.getOriginalFileName(),
                contract.getFileType(),
                contract.getFileSize(),
                contract.getS3Url(),
                contract.getUploadedBy() != null ? contract.getUploadedBy().getId() : null,
                contract.getUploadedBy() != null ? contract.getUploadedBy().getUsername() : null,
                contract.getUploadedAt(),
                contract.getStatus()
        );
    }
}
