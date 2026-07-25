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
import com.contractnegotiation.backend.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractServiceImplTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ContractServiceImpl contractService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");
    }

    @Test
    void uploadContract_success_usesProvidedTitleAndFilename() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("agreement.pdf");
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getBytes()).thenReturn("pdf-bytes".getBytes());
        when(multipartFile.getSize()).thenReturn(9L);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(s3Service.uploadFile(eq(multipartFile), anyString())).thenReturn("https://s3/agreement.pdf");

        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> {
            Contract c = invocation.getArgument(0);
            c.setId(100L);
            return c;
        });

        FileUploadResponseDto response = contractService.uploadContract(multipartFile, "My Title", "john");

        assertEquals("Contract PDF uploaded successfully", response.getMessage());
        assertEquals(100L, response.getContractId());
        assertEquals("My Title", response.getTitle());
        assertEquals("agreement.pdf", response.getFileName());
        assertEquals("application/pdf", response.getFileType());
        assertEquals(9L, response.getFileSize());
        assertEquals("https://s3/agreement.pdf", response.getS3Url());
        assertEquals(ContractStatus.UPLOADED, response.getStatus());

        ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository).save(captor.capture());
        assertEquals(user, captor.getValue().getUploadedBy());
    }

    @Test
    void uploadContract_blankTitle_fallsBackToFilename() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("agreement.pdf");
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getBytes()).thenReturn("bytes".getBytes());
        when(multipartFile.getSize()).thenReturn(5L);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(s3Service.uploadFile(eq(multipartFile), anyString())).thenReturn("https://s3/agreement.pdf");
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FileUploadResponseDto response = contractService.uploadContract(multipartFile, "   ", "john");

        assertEquals("agreement.pdf", response.getTitle());
    }

    @Test
    void uploadContract_missingOriginalFilename_defaultsToContractPdf() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getBytes()).thenReturn("bytes".getBytes());
        when(multipartFile.getSize()).thenReturn(5L);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(s3Service.uploadFile(eq(multipartFile), anyString())).thenReturn("https://s3/contract.pdf");
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FileUploadResponseDto response = contractService.uploadContract(multipartFile, null, "john");

        assertEquals("contract.pdf", response.getFileName());
        assertEquals("contract.pdf", response.getTitle());
    }

    @Test
    void uploadContract_nullContentType_defaultsToApplicationPdf() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("agreement.pdf");
        when(multipartFile.getContentType()).thenReturn(null);
        when(multipartFile.getBytes()).thenReturn("bytes".getBytes());
        when(multipartFile.getSize()).thenReturn(5L);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(s3Service.uploadFile(eq(multipartFile), anyString())).thenReturn("https://s3/agreement.pdf");
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FileUploadResponseDto response = contractService.uploadContract(multipartFile, "Title", "john");

        assertEquals("application/pdf", response.getFileType());
    }

    @Test
    void uploadContract_userNotFound_throwsResourceNotFoundException() {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("agreement.pdf");
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> contractService.uploadContract(multipartFile, "Title", "ghost"));

        verify(contractRepository, never()).save(any());
    }

    @Test
    void uploadContract_emptyFile_throwsInvalidFileException() {
        when(multipartFile.isEmpty()).thenReturn(true);

        assertThrows(InvalidFileException.class,
                () -> contractService.uploadContract(multipartFile, "Title", "john"));

        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void uploadContract_nullFile_throwsInvalidFileException() {
        assertThrows(InvalidFileException.class,
                () -> contractService.uploadContract(null, "Title", "john"));
    }

    @Test
    void uploadContract_nonPdfExtensionAndContentType_throwsInvalidFileException() {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("agreement.docx");
        when(multipartFile.getContentType()).thenReturn("application/msword");

        assertThrows(InvalidFileException.class,
                () -> contractService.uploadContract(multipartFile, "Title", "john"));
    }

    @Test
    void uploadContract_ioExceptionReadingBytes_throwsContractFileReadException() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("agreement.pdf");
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getBytes()).thenThrow(new IOException("disk error"));

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(s3Service.uploadFile(eq(multipartFile), anyString())).thenReturn("https://s3/agreement.pdf");

        assertThrows(ContractFileReadException.class,
                () -> contractService.uploadContract(multipartFile, "Title", "john"));
    }

    @Test
    void getContractById_found_returnsMappedDto() {
        Contract contract = buildContract(5L, user);
        when(contractRepository.findById(5L)).thenReturn(Optional.of(contract));

        ContractDto dto = contractService.getContractById(5L);

        assertEquals(5L, dto.getId());
        assertEquals("Title", dto.getTitle());
        assertEquals(1L, dto.getUploadedById());
        assertEquals("john", dto.getUploadedByUsername());
    }

    @Test
    void getContractById_notFound_throwsResourceNotFoundException() {
        when(contractRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contractService.getContractById(99L));
    }

    @Test
    void getContractById_noUploader_leavesUploaderFieldsNull() {
        Contract contract = buildContract(6L, null);
        when(contractRepository.findById(6L)).thenReturn(Optional.of(contract));

        ContractDto dto = contractService.getContractById(6L);

        assertThat(dto.getUploadedById()).isNull();
        assertThat(dto.getUploadedByUsername()).isNull();
    }

    @Test
    void getContractsByUserId_userExists_returnsMappedList() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(contractRepository.findByUploadedById(1L))
                .thenReturn(List.of(buildContract(1L, user), buildContract(2L, user)));

        List<ContractDto> result = contractService.getContractsByUserId(1L);

        assertEquals(2, result.size());
    }

    @Test
    void getContractsByUserId_userMissing_throwsResourceNotFoundException() {
        when(userRepository.existsById(42L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> contractService.getContractsByUserId(42L));

        verify(contractRepository, never()).findByUploadedById(any());
    }

    @Test
    void getAllContracts_returnsMappedList() {
        when(contractRepository.findAll()).thenReturn(List.of(buildContract(1L, user)));

        List<ContractDto> result = contractService.getAllContracts();

        assertEquals(1, result.size());
        assertEquals("Title", result.get(0).getTitle());
    }

    @Test
    void getAllContracts_empty_returnsEmptyList() {
        when(contractRepository.findAll()).thenReturn(List.of());

        List<ContractDto> result = contractService.getAllContracts();

        assertThat(result).isEmpty();
    }

    private Contract buildContract(Long id, User uploadedBy) {
        Contract contract = new Contract();
        contract.setId(id);
        contract.setTitle("Title");
        contract.setOriginalFileName("file.pdf");
        contract.setFileType("application/pdf");
        contract.setFileSize(10L);
        contract.setS3Url("https://s3/file.pdf");
        contract.setUploadedBy(uploadedBy);
        contract.setStatus(ContractStatus.UPLOADED);
        return contract;
    }
}
