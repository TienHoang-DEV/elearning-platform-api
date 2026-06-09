package vn.edu.fpt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.entity.User;
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

    public UserService(AzureBlobService azureBlobService, UserRepository repository, Validation validation) {
        this.azureBlobService = azureBlobService;
        this.repository = repository;
        this.validation = validation;
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


    public void updateProfileInstuctor(String email, String firstname, String lastname, String bio, String phone, MultipartFile file) {
        User user = repository.findByEmail(email).orElseThrow();
        if (firstname != null && !firstname.isEmpty() && lastname != null && !lastname.isEmpty()) {
            user.setFirstName(firstname);
            user.setLastName(lastname);
        }
        if (bio != null && !bio.isEmpty()) {
            user.setBio(bio);
        }

        if (validation.isValidPhone(phone)) {
            user.setPhone(phone);
        }

        if (file != null && !file.isEmpty()) {
            String url = azureBlobService.saveFile(file, "user-avatars");
            user.setAvatarUrl(url);
        }

        user.setUpdatedAt(LocalDateTime.now());
        repository.save(user);
    }
}
