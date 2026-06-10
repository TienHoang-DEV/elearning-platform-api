package vn.edu.fpt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.dto.InstructorRequestDTO;
import vn.edu.fpt.entity.InstructorRequest;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.InstructorRequestStatus;
import vn.edu.fpt.exception.BadRequestException;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.InstructorRequestRepository;
import vn.edu.fpt.repository.RoleRepository;
import vn.edu.fpt.util.AppConstants;
import vn.edu.fpt.util.SecurityUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InstructorRequestService {

    private final InstructorRequestRepository repository;
    private final RoleRepository roleRepository;
    private final DtoMapper dtoMapper;
    private final AzureBlobService azureBlobService;


    public InstructorRequestService(InstructorRequestRepository repository,
                                    RoleRepository roleRepository,
                                    DtoMapper dtoMapper,
                                    AzureBlobService azureBlobService) {

        this.repository = repository;
        this.roleRepository = roleRepository;
        this.dtoMapper = dtoMapper;
        this.azureBlobService = azureBlobService;

    }

    // ─────────────────────────────────────────────
    // CRUD cơ bản
    // ─────────────────────────────────────────────

    public List<InstructorRequest> findAll() {
        return repository.findAll();
    }

    public Optional<InstructorRequest> findById(Integer id) {
        return repository.findById(id);
    }

    public InstructorRequest save(InstructorRequest entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    // ─────────────────────────────────────────────
    // Nghiệp vụ cho Manager
    // ─────────────────────────────────────────────

    /**
     * Tìm kiếm và lọc danh sách yêu cầu, trả về Page<DTO>.
     */
    public Page<InstructorRequestDTO> searchAndFilter(String keyword, String statusStr, Pageable pageable) {
        InstructorRequestStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            status = InstructorRequestStatus.valueOf(statusStr.toUpperCase());
        }
        return repository.searchAndFilter(keyword, status, pageable).map(dtoMapper::toInstructorRequestDto);
    }

    /**
     * Lấy chi tiết một yêu cầu dưới dạng DTO.
     */
    public Optional<InstructorRequestDTO> findDtoById(Integer id) {
        return repository.findById(id).map(dtoMapper::toInstructorRequestDto);
    }

    public InstructorRequestDTO reviewRequest(Integer id, InstructorRequestStatus newStatus, String rejectionReason) {
        InstructorRequest request = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu với ID: " + id));

        if (request.getStatus() != InstructorRequestStatus.PENDING) {
            throw new BadRequestException("Yêu cầu đã được xử lý, không thể đổi trạng thái.");
        }

        if (newStatus == InstructorRequestStatus.APPROVED) {
            request.setStatus(InstructorRequestStatus.APPROVED);
            request.setRejectionReason(null);
            roleRepository.findByName("instructor")
                    .ifPresent(role -> request.getUser().getRoles().add(role));
            return dtoMapper.toInstructorRequestDto(repository.save(request));
        }

        if (newStatus == InstructorRequestStatus.REJECTED) {
            if (rejectionReason == null || rejectionReason.isBlank()) {
                throw new BadRequestException("Vui lòng nhập lý do từ chối.");
            }
            request.setStatus(InstructorRequestStatus.REJECTED);
            request.setRejectionReason(rejectionReason.trim());
            return dtoMapper.toInstructorRequestDto(repository.save(request));
        }
        throw new BadRequestException("Trạng thái xét duyệt không hợp lệ.");
    }

    //--Xử lý up load file


    public void submitRequest(
            InstructorRequestDTO dto,
            MultipartFile cvFile,
            MultipartFile idFront,
            MultipartFile idBack,
            MultipartFile certificateFiles
    ) {

        User currentUser = SecurityUtils.getCurrentUser();

        // Upload CV
        String cvUrl = azureBlobService.saveFile(
                cvFile,
                AppConstants.AZURE_STORAGE_CONTAINER_INSTRUCTOR_CVS
        );

        // Upload CCCD mặt trước
        String frontUrl = azureBlobService.saveFile(
                idFront,
                AppConstants.AZURE_STORAGE_CONTAINER_INSTRUCTOR_CVS
        );

        // Upload CCCD mặt sau
        String backUrl = azureBlobService.saveFile(
                idBack,
                AppConstants.AZURE_STORAGE_CONTAINER_INSTRUCTOR_CVS
        );

        String certificateUrl = azureBlobService.saveFile(certificateFiles,
                AppConstants.AZURE_STORAGE_CONTAINER_INSTRUCTOR_CVS);

        // Tạo request
        InstructorRequest request = new InstructorRequest();

        request.setUser(currentUser);
        request.setDescription(dto.getDescription());
        request.setBio(dto.getBio());

        request.setCvUrl(cvUrl);
        request.setNationalIdCardFront(frontUrl);
        request.setNationalIdCardBack(backUrl);
        request.setCertificateUrl(certificateUrl);

        request.setStatus(InstructorRequestStatus.PENDING);

        repository.save(request);


    }
}
