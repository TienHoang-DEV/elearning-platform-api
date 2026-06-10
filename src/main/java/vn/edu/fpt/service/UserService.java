package vn.edu.fpt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.dto.ProfileDto;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.exception.UserValidationException;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.util.Validation;

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

    public UserService(AzureBlobService azureBlobService, UserRepository repository, Validation validation, AuthService authService) {
        this.azureBlobService = azureBlobService;
        this.repository = repository;
        this.validation = validation;
        this.authService = authService;
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
}
