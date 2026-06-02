package vn.edu.fpt.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

	Optional<User> findByEmail(String email);
<<<<<<< HEAD
    User save(User u );

=======

	boolean existsByEmail(String email);
>>>>>>> feature/auth-backend-register-final
}
