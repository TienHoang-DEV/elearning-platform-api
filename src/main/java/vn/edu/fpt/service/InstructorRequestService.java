package vn.edu.fpt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.InstructorRequestDTO;
import vn.edu.fpt.entity.InstructorRequest;
import vn.edu.fpt.enums.InstructorRequestStatus;
import vn.edu.fpt.exception.BadRequestException;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.mapper.InstructorRequestMapper;
import vn.edu.fpt.repository.InstructorRequestRepository;
import vn.edu.fpt.repository.RoleRepository;


import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InstructorRequestService {

    private final InstructorRequestRepository repository;
    private final RoleRepository roleRepository;
    private final InstructorRequestMapper mapper;

    public InstructorRequestService(InstructorRequestRepository repository,
                                    RoleRepository roleRepository,
                                    InstructorRequestMapper mapper) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
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
        return repository.searchAndFilter(keyword, status, pageable).map(mapper::toDto);
    }

    /**
     * Lấy chi tiết một yêu cầu dưới dạng DTO.
     */
    public Optional<InstructorRequestDTO> findDtoById(Integer id) {
        return repository.findById(id).map(mapper::toDto);
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
            return mapper.toDto(repository.save(request));
        }

        if (newStatus == InstructorRequestStatus.REJECTED) {
            if (rejectionReason == null || rejectionReason.isBlank()) {
                throw new BadRequestException("Vui lòng nhập lý do từ chối.");
            }
            request.setStatus(InstructorRequestStatus.REJECTED);
            request.setRejectionReason(rejectionReason.trim());
            return mapper.toDto(repository.save(request));
        }
        throw new BadRequestException("Trạng thái xét duyệt không hợp lệ.");
    }
}
