package com.contractnegotiation.backend.service;

import com.contractnegotiation.backend.dto.ContractDto;
import com.contractnegotiation.backend.dto.FileUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContractService {

    FileUploadResponseDto uploadContract(MultipartFile file, String title, String username);

    ContractDto getContractById(Long id);

    List<ContractDto> getContractsByUserId(Long userId);

    List<ContractDto> getAllContracts();
}
