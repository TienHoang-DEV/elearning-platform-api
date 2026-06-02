package vn.edu.fpt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.InstructorRequestDTO;
import vn.edu.fpt.entity.InstructorRequest;
import vn.edu.fpt.enums.InstructorRequestStatus;
import vn.edu.fpt.mapper.InstructorRequestMapper;
import vn.edu.fpt.repository.InstructorRequestRepository;


import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InstructorRequestService {

    private final InstructorRequestRepository repository;
    private final InstructorRequestMapper mapper;

    public InstructorRequestService(InstructorRequestRepository repository,
                                    InstructorRequestMapper mapper) {
        this.repository = repository;
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
}
