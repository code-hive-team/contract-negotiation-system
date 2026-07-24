package com.contractnegotiation.backend.controller;

import com.contractnegotiation.backend.dto.ApiResponseDto;
import com.contractnegotiation.backend.dto.ContractDto;
import com.contractnegotiation.backend.dto.FileUploadResponseDto;
import com.contractnegotiation.backend.service.ContractService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<FileUploadResponseDto>> uploadContract(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            Authentication authentication) {

        String username = authentication.getName();
        FileUploadResponseDto result = contractService.uploadContract(file, title, username);

        ApiResponseDto<FileUploadResponseDto> response = new ApiResponseDto<>(
                HttpStatus.CREATED.value(),
                "PDF Contract uploaded successfully to S3 and recorded in database",
                result
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ContractDto>> getContractById(@PathVariable("id") Long id) {
        ContractDto contractDto = contractService.getContractById(id);
        ApiResponseDto<ContractDto> response = new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "Contract retrieved successfully",
                contractDto
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseDto<List<ContractDto>>> getContractsByUserId(@PathVariable("userId") Long userId) {
        List<ContractDto> contracts = contractService.getContractsByUserId(userId);
        ApiResponseDto<List<ContractDto>> response = new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "User contracts retrieved successfully",
                contracts
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<ContractDto>>> getAllContracts() {
        List<ContractDto> contracts = contractService.getAllContracts();
        ApiResponseDto<List<ContractDto>> response = new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "All contracts retrieved successfully",
                contracts
        );
        return ResponseEntity.ok(response);
    }
}
