package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.course.CategoryDto;
import vn.edu.fpt.entity.Category;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.entity.UserFavoriteCategory;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.CategoryRepository;
import vn.edu.fpt.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserFavoriteService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final DtoMapper dtoMapper;

    public UserFavoriteService(UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               DtoMapper dtoMapper) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.dtoMapper = dtoMapper;
    }

    public CategoryDto getActiveParentCategoryDto(Integer parentId) {
        Category parent = categoryRepository.findByIdAndStatus(parentId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return dtoMapper.toCategoryDto(parent);
    }

    public List<CategoryDto> getActiveChildrenCategoryDtos(Integer parentId) {
        List<Category> childEntities = categoryRepository.findByParentIdAndStatus(parentId, "ACTIVE");
        List<CategoryDto> children = new ArrayList<>();
        for (Category child : childEntities) {
            children.add(dtoMapper.toCategoryDto(child));
        }
        return children;
    }

    public User saveUserFavorites(User user, Integer parentId, List<Integer> childIds) {
        Set<Category> newFavorites = new HashSet<>();

        if (childIds == null || childIds.isEmpty()) {
            List<Category> allChildren = categoryRepository.findByParentIdAndStatus(parentId, "ACTIVE");
            if (allChildren != null && !allChildren.isEmpty()) {
                newFavorites.addAll(allChildren);
            } else {
                categoryRepository.findById(parentId).ifPresent(newFavorites::add);
            }
        } else {
            List<Category> selectedChildren = categoryRepository.findAllById(childIds);
            newFavorites.addAll(selectedChildren);
        }

        user.getFavoriteCategories().clear();
        for (Category c : newFavorites) {
            UserFavoriteCategory ufc = new UserFavoriteCategory();
            ufc.setUser(user);
            ufc.setCategory(c);
            user.getFavoriteCategories().add(ufc);
        }
        user.setFavoriteSetupCompleted(true);

        return userRepository.save(user);
    }
}
