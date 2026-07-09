package vn.edu.fpt.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.UserStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

	@Query("""
    SELECT DISTINCT u
    FROM User u
    LEFT JOIN FETCH u.userRoles ur
    LEFT JOIN FETCH ur.role
    WHERE u.email = :email
""")
	Optional<User> findByEmail(String email);

    User save(User u );


	boolean existsByEmail(String email);
	boolean existsByPhone(String phone);

	User findUserByPhone(String phone);

	@Query("SELECT u FROM User u " +
		   "JOIN u.userRoles ur " +
		   "JOIN ur.role r " +
		   "WHERE LOWER(r.name) = 'instructor' " +
		   "AND (:status IS NULL OR u.status = :status) " +
		   "AND (:keyword IS NULL OR :keyword = '' " +
		   "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		   "     OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		   "     OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
	Page<User> searchAndFilterInstructors(
			@Param("keyword") String keyword,
			@Param("status") UserStatus status,
			Pageable pageable);

	@Query("SELECT u FROM User u " +
		   "JOIN u.userRoles ur " +
		   "JOIN ur.role r " +
		   "WHERE LOWER(r.name) = 'manager' " +
		   "AND (:status IS NULL OR u.status = :status) " +
		   "AND (:keyword IS NULL OR :keyword = '' " +
		   "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		   "     OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		   "     OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
	Page<User> searchAndFilterManagers(
			@Param("keyword") String keyword,
			@Param("status") UserStatus status,
			Pageable pageable);
	@Query("SELECT COUNT(u) FROM User u JOIN u.userRoles ur JOIN ur.role r WHERE LOWER(r.name) = 'instructor'")
	long countInstructors();

	@Query("SELECT COUNT(u) FROM User u JOIN u.userRoles ur JOIN ur.role r WHERE LOWER(r.name) = 'learner'")
	long countLearners();

	boolean existsByEmailAndStatus(
			String email,
			UserStatus status
	);

	boolean existsByPhoneAndStatus(
			String phone,
			UserStatus status
	);
}


