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
    private final vn.edu.fpt.repository.CouponRepository couponRepository;
    private final vn.edu.fpt.repository.CartInstructorCouponRepository cartInstructorCouponRepository;

    public CartController(CartService cartService, CartItemService cartItemService,
                          CourseRepository courseRepository, UserRepository userRepository,
                          EnrollmentRepository enrollmentRepository,
                          DtoMapper dtoMapper,
                          vn.edu.fpt.repository.CouponRepository couponRepository,
                          vn.edu.fpt.repository.CartInstructorCouponRepository cartInstructorCouponRepository) {
        this.cartService = cartService;
        this.cartItemService = cartItemService;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.dtoMapper = dtoMapper;
        this.couponRepository = couponRepository;
        this.cartInstructorCouponRepository = cartInstructorCouponRepository;
    }

    private User getMockUser() {
        try {
            User currentUser = vn.edu.fpt.util.SecurityUtils.getCurrentUser();
            if (currentUser != null) {
                return userRepository.findById(currentUser.getId()).orElse(currentUser);
            }
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
        
        // Tính toán hóa đơn trên server
        long subtotal = 0;
        long courseDiscounts = 0;
        long instructorDiscounts = 0;
        long selectedItemsCount = 0;
        
        Map<Integer, String> appliedVoucherCodes = new HashMap<>();
        Map<Integer, Long> appliedVoucherDiscounts = new HashMap<>();
        Map<Integer, Boolean> voucherSuccess = new HashMap<>();
        Map<Integer, String> instructorCheckboxState = new HashMap<>(); // "checked", "unchecked", "indeterminate"
        
        for (Map.Entry<UserDto, List<CartItemDto>> entry : itemsByInstructor.entrySet()) {
            UserDto instructorDto = entry.getKey();
            List<CartItemDto> itemsList = entry.getValue();
            
            vn.edu.fpt.entity.CartInstructorCoupon appliedCoupon = cart.getInstructorCoupons().stream()
                .filter(cic -> cic.getInstructor().getId().equals(instructorDto.getId()))
                .findFirst()
                .orElse(null);
                
            long instSubtotal = 0;
            long instCourseDiscounts = 0;
            long groupSelectedCount = 0;
            
            for (CartItemDto item : itemsList) {
                if (item.isSelected()) {
                    long price = item.getCourse().getPrice().longValue();
                    long discount = Math.round(price * 0.3); // 30% discount
                    
                    subtotal += price;
                    courseDiscounts += discount;
                    selectedItemsCount++;
                    
                    instSubtotal += price;
                    instCourseDiscounts += discount;
                    groupSelectedCount++;
                }
            }
            
            // Xác định trạng thái checkbox của giảng viên
            if (groupSelectedCount == itemsList.size()) {
                instructorCheckboxState.put(instructorDto.getId(), "checked");
            } else if (groupSelectedCount == 0) {
                instructorCheckboxState.put(instructorDto.getId(), "unchecked");
            } else {
                instructorCheckboxState.put(instructorDto.getId(), "indeterminate");
            }
            
            if (appliedCoupon != null) {
                vn.edu.fpt.entity.Coupon coupon = appliedCoupon.getCoupon();
                appliedVoucherCodes.put(instructorDto.getId(), coupon.getCode());
                
                if (groupSelectedCount > 0) {
                    long instSubtotalAfterDiscount = instSubtotal - instCourseDiscounts;
                    long instDiscountAmount = 0;
                    if ("PERCENT".equalsIgnoreCase(coupon.getDiscountType())) {
                        double rate = coupon.getDiscountValue().doubleValue() / 100.0;
                        instDiscountAmount = Math.round(instSubtotalAfterDiscount * rate);
                    } else if ("FIXED".equalsIgnoreCase(coupon.getDiscountType())) {
                        instDiscountAmount = coupon.getDiscountValue().longValue();
                        if (instDiscountAmount > instSubtotalAfterDiscount) {
                            instDiscountAmount = instSubtotalAfterDiscount;
                        }
                    }
                    instructorDiscounts += instDiscountAmount;
                    appliedVoucherDiscounts.put(instructorDto.getId(), instDiscountAmount);
                    voucherSuccess.put(instructorDto.getId(), true);
                } else {
                    voucherSuccess.put(instructorDto.getId(), false);
                }
            }
        }
        
        long total = subtotal - courseDiscounts - instructorDiscounts;
        if (total < 0) total = 0;
        
        boolean allSelected = cart.getItems().isEmpty() ? false : cart.getItems().stream().allMatch(vn.edu.fpt.entity.CartItem::isSelected);
        boolean noneSelected = cart.getItems().stream().noneMatch(vn.edu.fpt.entity.CartItem::isSelected);
        String globalCheckboxState = allSelected ? "checked" : (noneSelected ? "unchecked" : "indeterminate");
        
        model.addAttribute("cart", cartDto);
        model.addAttribute("itemsByInstructor", itemsByInstructor);
        model.addAttribute("cartSize", cartSize);
        
        // Thêm thông tin hóa đơn vào Model
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("courseDiscounts", courseDiscounts);
        model.addAttribute("instructorDiscounts", instructorDiscounts);
        model.addAttribute("total", total);
        model.addAttribute("selectedItemsCount", selectedItemsCount);
        
        model.addAttribute("appliedVoucherCodes", appliedVoucherCodes);
        model.addAttribute("appliedVoucherDiscounts", appliedVoucherDiscounts);
        model.addAttribute("voucherSuccess", voucherSuccess);
        model.addAttribute("instructorCheckboxState", instructorCheckboxState);
        model.addAttribute("globalCheckboxState", globalCheckboxState);
        
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

            java.util.List<CartItem> selectedItems = items.stream()
                    .filter(CartItem::isSelected)
                    .collect(Collectors.toList());

            if (selectedItems.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng chọn ít nhất một khóa học để thanh toán.");
                return ResponseEntity.ok(response);
            }

            for (CartItem item : selectedItems) {
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
                cart.removeItem(item);
                cartItemService.deleteById(item.getId());
            }

            // Xóa các applied coupons của các giảng viên không còn khóa học nào của họ trong giỏ hàng
            java.util.Set<User> remainingInstructors = cart.getItems().stream()
                    .map(item -> item.getCourse().getInstructor())
                    .collect(Collectors.toSet());
            
            java.util.List<vn.edu.fpt.entity.CartInstructorCoupon> couponsToRemove = cart.getInstructorCoupons().stream()
                    .filter(cic -> !remainingInstructors.contains(cic.getInstructor()))
                    .collect(Collectors.toList());
            
            for (vn.edu.fpt.entity.CartInstructorCoupon cic : couponsToRemove) {
                cart.removeInstructorCoupon(cic);
            }

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

    @PostMapping("/api/cart/toggle-select")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSelect(@RequestParam("cartItemId") Integer cartItemId,
                                                            @RequestParam(value = "selected", required = false) Boolean selected) {
        Map<String, Object> response = new HashMap<>();
        try {
            CartItem item = cartItemService.findById(cartItemId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ hàng."));
            if (selected != null) {
                item.setSelected(selected);
            } else {
                item.setSelected(!item.isSelected());
            }
            cartItemService.save(item);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/api/cart/toggle-select-instructor")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSelectInstructor(@RequestParam("instructorId") Integer instructorId,
                                                                      @RequestParam("selected") Boolean selected) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getMockUser();
            Cart cart = cartService.getOrCreateCartForUser(user);
            for (CartItem item : cart.getItems()) {
                if (item.getCourse() != null && item.getCourse().getInstructor() != null 
                        && item.getCourse().getInstructor().getId().equals(instructorId)) {
                    item.setSelected(selected);
                    cartItemService.save(item);
                }
            }
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/api/cart/toggle-select-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSelectAll(@RequestParam("selected") Boolean selected) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getMockUser();
            Cart cart = cartService.getOrCreateCartForUser(user);
            for (CartItem item : cart.getItems()) {
                item.setSelected(selected);
                cartItemService.save(item);
            }
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/api/cart/apply-voucher")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyVoucher(@RequestParam("instructorId") Integer instructorId,
                                                            @RequestParam("code") String code) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getMockUser();
            Cart cart = cartService.getOrCreateCartForUser(user);
            
            String trimmedCode = code.trim();
            if (trimmedCode.isEmpty()) {
                // Xóa voucher của giảng viên này nếu nhập trống
                cart.getInstructorCoupons().removeIf(cic -> cic.getInstructor().getId().equals(instructorId));
                cartService.save(cart);
                response.put("success", true);
                response.put("message", "Đã gỡ bỏ mã giảm giá.");
                return ResponseEntity.ok(response);
            }

            // Tìm coupon trong database
            vn.edu.fpt.entity.Coupon coupon = couponRepository.findByCode(trimmedCode)
                    .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không hợp lệ!"));

            // Xác thực coupon
            if (coupon.getInstructor() == null || !coupon.getInstructor().getId().equals(instructorId)) {
                throw new IllegalArgumentException("Mã giảm giá này không thuộc về giảng viên hiện tại.");
            }
            if ("INACTIVE".equalsIgnoreCase(coupon.getStatus())) {
                throw new IllegalArgumentException("Mã giảm giá đã bị vô hiệu hóa.");
            }
            if (coupon.getExpiredAt() != null && coupon.getExpiredAt().isBefore(java.time.LocalDateTime.now())) {
                throw new IllegalArgumentException("Mã giảm giá đã hết hạn sử dụng.");
            }
            if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
                throw new IllegalArgumentException("Mã giảm giá đã hết số lần sử dụng.");
            }

            // Kiểm tra xem người dùng có chọn ít nhất một khóa học của giảng viên này không
            boolean hasSelectedCourse = cart.getItems().stream()
                    .anyMatch(item -> item.isSelected() 
                            && item.getCourse().getInstructor() != null 
                            && item.getCourse().getInstructor().getId().equals(instructorId));

            if (!hasSelectedCourse) {
                throw new IllegalArgumentException("Vui lòng tích chọn ít nhất một khóa học của giảng viên này để áp dụng mã!");
            }

            // Lưu coupon vào Cart
            // Xóa coupon cũ của giảng viên này trong giỏ hàng nếu có
            cart.getInstructorCoupons().removeIf(cic -> cic.getInstructor().getId().equals(instructorId));
            
            vn.edu.fpt.entity.CartInstructorCoupon newCic = vn.edu.fpt.entity.CartInstructorCoupon.builder()
                    .cart(cart)
                    .instructor(coupon.getInstructor())
                    .coupon(coupon)
                    .build();
            cart.addInstructorCoupon(newCic);
            cartService.save(cart);

            response.put("success", true);
            response.put("message", "Áp dụng mã giảm giá thành công!");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}