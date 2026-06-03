package vn.edu.fpt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.entity.Cart;
import vn.edu.fpt.entity.CartItem;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.entity.Enrollment;
import vn.edu.fpt.repository.CourseRepository;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.repository.EnrollmentRepository;
import vn.edu.fpt.service.CartItemService;
import vn.edu.fpt.service.CartService;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@org.springframework.transaction.annotation.Transactional
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final DtoMapper dtoMapper;

    public CartController(CartService cartService, CartItemService cartItemService,
                          CourseRepository courseRepository, UserRepository userRepository,
                          EnrollmentRepository enrollmentRepository,
                          DtoMapper dtoMapper) {
        this.cartService = cartService;
        this.cartItemService = cartItemService;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.dtoMapper = dtoMapper;
    }

    private User getMockUser() {
        try {
            jakarta.servlet.http.HttpServletRequest request = 
                ((org.springframework.web.context.request.ServletRequestAttributes) 
                 org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes())
                .getRequest();
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            if (session != null) {
                User sessionUser = (User) session.getAttribute("user");
                if (sessionUser != null) {
                    return userRepository.findById(sessionUser.getId()).orElse(sessionUser);
                }
            }
        } catch (Exception ignored) {
        }
        return userRepository.findByEmail("28tech@gmail.com")
                .orElseGet(() -> {
                    List<User> allUsers = userRepository.findAll();
                    if (allUsers.isEmpty()) {
                        throw new IllegalStateException("Không tìm thấy người dùng nào trong cơ sở dữ liệu để giả lập. Vui lòng import lại file sql_ddl_dml/ElearningPlatform.sql vào SQL Server của bạn!");
                    }
                    return allUsers.get(0);
                });
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping("/cart")
    public String showCartPage(Model model) {
        User user = getMockUser();
        Cart cart = cartService.getOrCreateCartForUser(user);
        
        CartDto cartDto = dtoMapper.toCartDto(cart);

        // Nhóm các CartItemDto theo Giảng viên của khóa học
        Map<UserDto, List<CartItemDto>> itemsByInstructor = new HashMap<>();
        if (cartDto.getItems() != null) {
            for (CartItemDto item : cartDto.getItems()) {
                if (item.getCourse() != null && item.getCourse().getInstructor() != null) {
                    UserDto instructor = item.getCourse().getInstructor();
                    if (!itemsByInstructor.containsKey(instructor)) {
                        itemsByInstructor.put(instructor, new java.util.ArrayList<>());
                    }
                    itemsByInstructor.get(instructor).add(item);
                }
            }
        }
        
        int cartSize = cartItemService.countItemsInCart(cart);
        
        model.addAttribute("cart", cartDto);
        model.addAttribute("itemsByInstructor", itemsByInstructor);
        model.addAttribute("cartSize", cartSize);
        
        return "cart/cart";
    }

    @PostMapping("/api/cart/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addCourseToCart(@RequestParam("courseId") Integer courseId) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getMockUser();
            Cart cart = cartService.getOrCreateCartForUser(user);
            
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khóa học với ID: " + courseId));

            boolean newlyAdded = cartItemService.addCourseToCart(cart, course);
            int cartSize = cartItemService.countItemsInCart(cart);

            response.put("success", true);
            if (newlyAdded) {
                response.put("message", "Đã thêm khóa học vào giỏ hàng thành công!");
            } else {
                response.put("message", "Khóa học này đã có sẵn trong giỏ hàng.");
            }
            response.put("cartSize", cartSize);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/api/cart/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeCartItem(@RequestParam("cartItemId") Integer cartItemId) {
        Map<String, Object> response = new HashMap<>();
        try {
            cartItemService.deleteById(cartItemId);
            response.put("success", true);
            response.put("message", "Đã xóa khóa học khỏi giỏ hàng.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi xóa: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/cart/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartCount() {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getMockUser();
            Cart cart = cartService.getOrCreateCartForUser(user);
            int cartSize = cartItemService.countItemsInCart(cart);
            
            response.put("success", true);
            response.put("cartSize", cartSize);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("cartSize", 0);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/api/cart/checkout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkoutCart() {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getMockUser();
            Cart cart = cartService.getOrCreateCartForUser(user);
            java.util.Set<CartItem> items = cart.getItems();

            if (items == null || items.isEmpty()) {
                response.put("success", false);
                response.put("message", "Giỏ hàng rỗng.");
                return ResponseEntity.ok(response);
            }

            java.util.List<CartItem> itemsList = new java.util.ArrayList<>(items);
            for (CartItem item : itemsList) {
                Course course = item.getCourse();
                boolean alreadyEnrolled = enrollmentRepository.existsByUserAndCourse(user, course);
                if (!alreadyEnrolled) {
                    Enrollment enrollment = Enrollment.builder()
                            .user(user)
                            .course(course)
                            .progressPercent(java.math.BigDecimal.ZERO)
                            .build();
                    enrollmentRepository.save(enrollment);
                }
                cartItemService.deleteById(item.getId());
            }

            cart.getItems().clear();
            cartService.save(cart);

            response.put("success", true);
            response.put("message", "Thanh toán thành công! Khóa học đã được thêm vào Việc Học Của Tôi.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Thanh toán thất bại: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}