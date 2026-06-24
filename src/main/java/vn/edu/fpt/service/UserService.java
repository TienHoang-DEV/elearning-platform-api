package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.user.ProfileDto;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.exception.UserValidationException;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.service.cloud.AzureBlobService;
import vn.edu.fpt.util.Validation;

import vn.edu.fpt.dto.user.UserDto;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.enums.UserStatus;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.CourseRepository;
import vn.edu.fpt.exception.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {


    private AzureBlobService azureBlobService;
    private UserRepository repository;
    private Validation validation;
    private AuthService authService;
    private DtoMapper dtoMapper;
    private CourseRepository courseRepository;

    public UserService(AzureBlobService azureBlobService, UserRepository repository,
                       Validation validation, AuthService authService,
                       DtoMapper dtoMapper, CourseRepository courseRepository) {
        this.azureBlobService = azureBlobService;
        this.repository = repository;
        this.validation = validation;
        this.authService = authService;
        this.dtoMapper = dtoMapper;
        this.courseRepository = courseRepository;
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public User findById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
    }

    public User save(User entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }



    public void updateProfileInstuctor(User user, ProfileDto profileDto) {

        User tmp = repository.findUserByPhone(profileDto.getPhone());
        if (tmp != null && !tmp.getId().equals(user.getId())) {
            throw new UserValidationException("phone","Số điện thoại này đã được sử dụng.");
        }


        if (profileDto.getFile() != null && !profileDto.getFile().isEmpty()) {
            if (profileDto.getFile().getSize() > 2 * 1024 * 1024) {
                throw new UserValidationException("file","Vui lòng chọn ảnh có kích thước không vượt quá 2MB.");
            }

            String url = azureBlobService.saveFile(profileDto.getFile(), "user-avatars");
            user.setAvatarUrl(url);
        }

        user.setFirstName(profileDto.getFirstname().trim());
        user.setLastName(profileDto.getLastname().trim());
        user.setBio(profileDto.getBio().trim());
        user.setPhone(profileDto.getPhone().trim());
        user.setUpdatedAt(LocalDateTime.now());

        repository.save(user);
    }

    // =========================================================================
    // ACADEMIC MANAGER (QUẢN LÝ HỌC THUẬT) SECTION
    // =========================================================================

    /**
     * Tìm kiếm và lọc danh sách giảng viên (hỗ trợ phân trang).
     */
    public Page<UserDto> searchAndFilterInstructors(String keyword, UserStatus status, Pageable pageable) {
        Page<User> instructorPage = repository.searchAndFilterInstructors(keyword, status, pageable);
        return instructorPage.map(dtoMapper::toUserDto);
    }

    /**
     * Lấy thông tin chi tiết giảng viên dưới dạng DTO.
     */
    public UserDto getInstructorDetail(Integer id) {
        User instructor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên với ID: " + id));
        return dtoMapper.toUserDto(instructor);
    }

    /**
     * Lấy danh sách khóa học của một giảng viên.
     */
    public List<Course> getInstructorCourses(Integer instructorId) {
        User instructor = repository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên với ID: " + instructorId));
        return courseRepository.findByInstructor(instructor);
    }

    /**
     * Cập nhật trạng thái tài khoản giảng viên (ACTIVE / BANNED).
     */
    public void updateInstructorStatus(Integer id, UserStatus status) {
        User instructor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên với ID: " + id));
        instructor.setStatus(status);
        repository.save(instructor);
    }
}
