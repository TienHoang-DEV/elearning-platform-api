package vn.edu.fpt.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vn.edu.fpt.dto.CategoryDto;
import vn.edu.fpt.dto.UserDto;
import vn.edu.fpt.entity.Cart;
import vn.edu.fpt.entity.CartItem;
import vn.edu.fpt.entity.Category;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.CourseLevel;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.service.CartItemService;
import vn.edu.fpt.service.CartService;
import vn.edu.fpt.service.CategoryService;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;
    private final CartItemService cartItemService;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final DtoMapper dtoMapper;

    public GlobalControllerAdvice(CartService cartService, CartItemService cartItemService, UserRepository userRepository, CategoryService categoryService, DtoMapper dtoMapper) {
        this.cartService = cartService;
        this.cartItemService = cartItemService;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.dtoMapper = dtoMapper;
    }

    private boolean isHtmlRequest(HttpServletRequest request) {
        if (request == null) {
            return true;
        }
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/") || uri.startsWith("/api") || uri.contains("/material/url")) {
            return false;
        }
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }

    @ModelAttribute("headerCategories")
    public List<CategoryDto> getHeaderCategories(HttpServletRequest request) {
        if (!isHtmlRequest(request)) {
            return List.of();
        }
        try {
            return categoryService.getActiveParentCategories();
        } catch (Exception ignored) {
        }
        return List.of();
    }

    @ModelAttribute("currentUser")
    public UserDto getCurrentUser(HttpServletRequest request) {
        if (!isHtmlRequest(request)) {
            return null;
        }
        try {
            User user = vn.edu.fpt.util.SecurityUtils.getCurrentUser();
            if (user == null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    User sessionUser = (User) session.getAttribute("user");
                    if (sessionUser != null) {
                        user = userRepository.findById(sessionUser.getId()).orElse(null);
                    }
                }
            }
            return user != null ? dtoMapper.toUserDto(user) : null;
        } catch (Exception ignored) {
        }
        return null;
    }

    @ModelAttribute("cartSize")
    public int getCartSize(HttpServletRequest request) {
        if (!isHtmlRequest(request)) {
            return 0;
        }
        try {
            User user = vn.edu.fpt.util.SecurityUtils.getCurrentUser();
            if (user == null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    User sessionUser = (User) session.getAttribute("user");
                    if (sessionUser != null) {
                        user = userRepository.findById(sessionUser.getId()).orElse(null);
                    }
                }
            }
            if (user != null) {
                Cart cart = cartService.getOrCreateCartForUser(user);
                return cartItemService.countItemsInCart(cart);
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

}
