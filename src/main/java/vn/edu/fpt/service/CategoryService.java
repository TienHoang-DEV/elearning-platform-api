package vn.edu.fpt.service;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.Category;
import vn.edu.fpt.enums.CategoryStatus;
import vn.edu.fpt.repository.CategoryRepository;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.dto.CategoryDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository repository;
    private final DtoMapper dtoMapper;

    public CategoryService(CategoryRepository categoryRepository, DtoMapper dtoMapper) {
        this.repository = categoryRepository;
        this.dtoMapper = dtoMapper;
    }

    public List<CategoryDto> getActiveParentCategories() {
        List<Category> parentCategories = repository.findByParentIsNullAndStatus("ACTIVE");
        List<CategoryDto> dtos = new java.util.ArrayList<>();
        for (Category category : parentCategories) {
            dtos.add(dtoMapper.toCategoryDto(category));
        }
        return dtos;
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
