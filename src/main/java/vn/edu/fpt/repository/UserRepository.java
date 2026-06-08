package vn.edu.fpt.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

	@Query("""
    SELECT DISTINCT u
    FROM User u
    LEFT JOIN FETCH u.roles
    WHERE u.email = :email
""")
	Optional<User> findByEmail(String email);

    User save(User u );


	boolean existsByEmail(String email);

	User findUserByPhone(String phone);

}
