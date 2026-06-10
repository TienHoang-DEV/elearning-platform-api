package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Category;
import vn.edu.fpt.enums.CategoryStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByParentIsNullAndStatus(String status);


    List<Category> findByParentIsNotNullAndStatus(String status);


    Optional<Category> findByIdAndStatus(Integer id, String status);


}
