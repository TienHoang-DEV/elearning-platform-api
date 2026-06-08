package vn.edu.fpt.service;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.Category;
import vn.edu.fpt.enums.CategoryStatus;
import vn.edu.fpt.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.repository = categoryRepository;
    }

    public List<Category> findAll() {
        return repository.findAll();
    }

    @Query()
    public List<Category> findByParentIsNullAndStatus(String status){
        return repository.findByParentIsNullAndStatus(status);
    };

    public Category findByIdAndStatus(Integer id, String status) {
        return repository.findByIdAndStatus(id, status)
                .orElseThrow(() -> new RuntimeException("Category not found or inactive"));
    }


    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public Category save(Category entity) {
        return repository.save(entity);
    }


}
