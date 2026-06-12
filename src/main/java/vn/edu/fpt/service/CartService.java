package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.*;
import vn.edu.fpt.dto.*;
import vn.edu.fpt.repository.*;
import vn.edu.fpt.mapper.DtoMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {
    private final CartRepository repository;
    private final CartItemService cartItemService;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final DtoMapper dtoMapper;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;

    public CartService(CartRepository cartRepository,
                       CartItemService cartItemService,
                       CourseRepository courseRepository,
                       UserRepository userRepository,
                       EnrollmentRepository enrollmentRepository,
                       DtoMapper dtoMapper,
                       CouponRepository couponRepository,
                       OrderRepository orderRepository) {
        this.repository = cartRepository;
        this.cartItemService = cartItemService;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.dtoMapper = dtoMapper;
        this.couponRepository = couponRepository;
        this.orderRepository = orderRepository;
    }

    public Cart getOrCreateCartForUser(User user) {
        return repository.findByUser(user)
                .orElseGet(() -> repository.save(Cart.builder().user(user).build()));
    }

    public List<Cart> findAll() { return repository.findAll(); }
    public Optional<Cart> findById(Integer id) { return repository.findById(id); }
    public Cart save(Cart entity) { return repository.save(entity); }
    public void deleteById(Integer id) { repository.deleteById(id); }
    public boolean existsById(Integer id) { return repository.existsById(id); }

    public CartPageDetailsDto getCartPageDetails(User user) {
        Cart cart = getOrCreateCartForUser(user);
        CartDto cartDto = dtoMapper.toCartDto(cart);

        // Nhóm các CartItemDto theo Giảng viên của khóa học
        Map<UserDto, List<CartItemDto>> itemsByInstructor = new HashMap<>();
        if (cartDto.getItems() != null) {
            for (CartItemDto item : cartDto.getItems()) {
                if (item.getCourse() != null && item.getCourse().getInstructor() != null) {
                    UserDto instructor = item.getCourse().getInstructor();
                    if (!itemsByInstructor.containsKey(instructor)) {
                        itemsByInstructor.put(instructor, new ArrayList<>());
                    }
                    itemsByInstructor.get(instructor).add(item);
                }
            }
        }

        int cartSize = cartItemService.countItemsInCart(cart);

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

            CartInstructorCoupon appliedCoupon = null;
            for (CartInstructorCoupon cic : cart.getInstructorCoupons()) {
                if (cic.getInstructor() != null && cic.getInstructor().getId().equals(instructorDto.getId())) {
                    appliedCoupon = cic;
                    break;
                }
            }

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
                Coupon coupon = appliedCoupon.getCoupon();
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

        boolean allSelected = true;
        boolean noneSelected = true;

        if (cart.getItems().isEmpty()) {
            allSelected = false;
        } else {
            for (CartItem item : cart.getItems()) {
                if (item.isSelected()) {
                    noneSelected = false;
                } else {
                    allSelected = false;
                }
            }
        }
        String globalCheckboxState = allSelected ? "checked" : (noneSelected ? "unchecked" : "indeterminate");

        return CartPageDetailsDto.builder()
                .cart(cartDto)
                .itemsByInstructor(itemsByInstructor)
                .cartSize(cartSize)
                .subtotal(subtotal)
                .courseDiscounts(courseDiscounts)
                .instructorDiscounts(instructorDiscounts)
                .total(total)
                .selectedItemsCount(selectedItemsCount)
                .appliedVoucherCodes(appliedVoucherCodes)
                .appliedVoucherDiscounts(appliedVoucherDiscounts)
                .voucherSuccess(voucherSuccess)
                .instructorCheckboxState(instructorCheckboxState)
                .globalCheckboxState(globalCheckboxState)
                .build();
    }

    public Map<String, Object> addCourseToCart(User user, Integer courseId) {
        Cart cart = getOrCreateCartForUser(user);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khóa học với ID: " + courseId));

        boolean newlyAdded = cartItemService.addCourseToCart(cart, course);
        int cartSize = cartItemService.countItemsInCart(cart);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("newlyAdded", newlyAdded);
        response.put("message", newlyAdded ? "Đã thêm khóa học vào giỏ hàng thành công!" : "Khóa học này đã có sẵn trong giỏ hàng.");
        response.put("cartSize", cartSize);
        return response;
    }

    public void removeCartItem(Integer cartItemId) {
        cartItemService.deleteById(cartItemId);
    }

    public int getCartCount(User user) {
        Cart cart = getOrCreateCartForUser(user);
        return cartItemService.countItemsInCart(cart);
    }

    public void toggleSelect(Integer cartItemId, Boolean selected) {
        CartItem item = cartItemService.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ hàng."));
        if (selected != null) {
            item.setSelected(selected);
        } else {
            item.setSelected(!item.isSelected());
        }
        cartItemService.save(item);
    }

    public void toggleSelectInstructor(User user, Integer instructorId, Boolean selected) {
        Cart cart = getOrCreateCartForUser(user);
        for (CartItem item : cart.getItems()) {
            if (item.getCourse() != null && item.getCourse().getInstructor() != null 
                    && item.getCourse().getInstructor().getId().equals(instructorId)) {
                item.setSelected(selected);
                cartItemService.save(item);
            }
        }
    }

    public void toggleSelectAll(User user, Boolean selected) {
        Cart cart = getOrCreateCartForUser(user);
        for (CartItem item : cart.getItems()) {
            item.setSelected(selected);
            cartItemService.save(item);
        }
    }

    public Map<String, Object> applyVoucher(User user, Integer instructorId, String code) {
        Map<String, Object> response = new HashMap<>();
        Cart cart = getOrCreateCartForUser(user);

        String trimmedCode = code.trim();
        if (trimmedCode.isEmpty()) {
            // Xóa voucher của giảng viên này nếu nhập trống bằng Iterator truyền thống
            java.util.Iterator<CartInstructorCoupon> iterator = cart.getInstructorCoupons().iterator();
            while (iterator.hasNext()) {
                CartInstructorCoupon cic = iterator.next();
                if (cic.getInstructor() != null && cic.getInstructor().getId().equals(instructorId)) {
                    iterator.remove();
                }
            }
            save(cart);
            response.put("success", true);
            response.put("message", "Đã gỡ bỏ mã giảm giá.");
            return response;
        }

        // Tìm coupon trong database
        Coupon coupon = couponRepository.findByCode(trimmedCode)
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
        boolean hasSelectedCourse = false;
        for (CartItem item : cart.getItems()) {
            if (item.isSelected()
                    && item.getCourse() != null
                    && item.getCourse().getInstructor() != null
                    && item.getCourse().getInstructor().getId().equals(instructorId)) {
                hasSelectedCourse = true;
                break;
            }
        }

        if (!hasSelectedCourse) {
            throw new IllegalArgumentException("Vui lòng tích chọn ít nhất một khóa học của giảng viên này để áp dụng mã!");
        }

        // Lưu coupon vào Cart: Trước tiên loại bỏ coupon cũ của giảng viên này bằng Iterator truyền thống
        java.util.Iterator<CartInstructorCoupon> iterator = cart.getInstructorCoupons().iterator();
        while (iterator.hasNext()) {
            CartInstructorCoupon cic = iterator.next();
            if (cic.getInstructor() != null && cic.getInstructor().getId().equals(instructorId)) {
                iterator.remove();
            }
        }

        CartInstructorCoupon newCic = CartInstructorCoupon.builder()
                .cart(cart)
                .instructor(coupon.getInstructor())
                .coupon(coupon)
                .build();
        cart.addInstructorCoupon(newCic);
        save(cart);

        response.put("success", true);
        response.put("message", "Áp dụng mã giảm giá thành công!");
        return response;
    }

    public Map<String, Object> checkoutCart(User user) {
        Map<String, Object> response = new HashMap<>();
        Cart cart = getOrCreateCartForUser(user);
        Set<CartItem> items = cart.getItems();

        if (items == null || items.isEmpty()) {
            response.put("success", false);
            response.put("message", "Giỏ hàng rỗng.");
            return response;
        }

        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : items) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            response.put("success", false);
            response.put("message", "Vui lòng chọn ít nhất một khóa học để thanh toán.");
            return response;
        }

        // Group selected items by instructor for coupon calculations
        Map<User, List<CartItem>> itemsByInstructor = new HashMap<>();
        for (CartItem item : selectedItems) {
            Course course = item.getCourse();
            if (course != null && course.getInstructor() != null) {
                User instructor = course.getInstructor();
                if (!itemsByInstructor.containsKey(instructor)) {
                    itemsByInstructor.put(instructor, new ArrayList<>());
                }
                itemsByInstructor.get(instructor).add(item);
            }
        }

        List<OrderItem> orderItems = new ArrayList<>();
        java.math.BigDecimal orderSubtotal = java.math.BigDecimal.ZERO;
        java.math.BigDecimal orderTotalDiscount = java.math.BigDecimal.ZERO;

        for (Map.Entry<User, List<CartItem>> entry : itemsByInstructor.entrySet()) {
            User instructor = entry.getKey();
            List<CartItem> itemList = entry.getValue();

            // Find coupon for this instructor
            CartInstructorCoupon appliedCic = null;
            for (CartInstructorCoupon cic : cart.getInstructorCoupons()) {
                if (cic.getInstructor() != null && cic.getInstructor().getId().equals(instructor.getId())) {
                    appliedCic = cic;
                    break;
                }
            }

            Coupon coupon = (appliedCic != null) ? appliedCic.getCoupon() : null;

            // Calculate base values
            long instSubtotal = 0;
            long instCourseDiscounts = 0;
            for (CartItem item : itemList) {
                long price = item.getCourse().getPrice().longValue();
                long courseDiscount = Math.round(price * 0.3); // 30% default discount
                instSubtotal += price;
                instCourseDiscounts += courseDiscount;
            }

            long instSubtotalAfterDiscount = instSubtotal - instCourseDiscounts;
            long voucherDiscount = 0;
            if (coupon != null) {
                if ("PERCENT".equalsIgnoreCase(coupon.getDiscountType())) {
                    double rate = coupon.getDiscountValue().doubleValue() / 100.0;
                    voucherDiscount = Math.round(instSubtotalAfterDiscount * rate);
                } else if ("FIXED".equalsIgnoreCase(coupon.getDiscountType())) {
                    voucherDiscount = coupon.getDiscountValue().longValue();
                    if (voucherDiscount > instSubtotalAfterDiscount) {
                        voucherDiscount = instSubtotalAfterDiscount;
                    }
                }
            }

            // Distribute voucher discount across items
            long remainingVoucherDiscount = voucherDiscount;
            for (int i = 0; i < itemList.size(); i++) {
                CartItem item = itemList.get(i);
                long price = item.getCourse().getPrice().longValue();
                long baseCourseDiscount = Math.round(price * 0.3);
                long itemSubtotalAfterBase = price - baseCourseDiscount;

                long distributedVoucherDiscount = 0;
                if (coupon != null && instSubtotalAfterDiscount > 0) {
                    if (i == itemList.size() - 1) {
                        distributedVoucherDiscount = remainingVoucherDiscount;
                    } else {
                        distributedVoucherDiscount = Math.round((double) voucherDiscount * itemSubtotalAfterBase / instSubtotalAfterDiscount);
                        remainingVoucherDiscount -= distributedVoucherDiscount;
                    }
                }

                long totalItemDiscount = baseCourseDiscount + distributedVoucherDiscount;
                long finalPrice = price - totalItemDiscount;
                if (finalPrice < 0) {
                    finalPrice = 0;
                }

                OrderItem orderItem = OrderItem.builder()
                        .coupon(coupon)
                        .priceSnapshot(item.getCourse().getPrice())
                        .discountAmount(java.math.BigDecimal.valueOf(totalItemDiscount))
                        .finalPrice(java.math.BigDecimal.valueOf(finalPrice))
                        .courseTitleSnapshot(item.getCourse().getTitle())
                        .course(item.getCourse())
                        .build();

                orderItems.add(orderItem);
                orderSubtotal = orderSubtotal.add(item.getCourse().getPrice());
                orderTotalDiscount = orderTotalDiscount.add(java.math.BigDecimal.valueOf(totalItemDiscount));
            }
        }

        java.math.BigDecimal orderTotalAmount = orderSubtotal.subtract(orderTotalDiscount);
        if (orderTotalAmount.compareTo(java.math.BigDecimal.ZERO) < 0) {
            orderTotalAmount = java.math.BigDecimal.ZERO;
        }

        // Create and save the Order
        Order order = Order.builder()
                .user(user)
                .totalAmount(orderTotalAmount)
                .discountAmount(orderTotalDiscount)
                .status("paid")
                .paymentMethod("ATM / Internet Banking")
                .build();

        for (OrderItem oi : orderItems) {
            order.addItem(oi);
        }

        orderRepository.save(order);

        // Process enrollments and remove from cart
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

        // Clean up applied coupons
        Set<User> remainingInstructors = cart.getItems().stream()
                .map(item -> item.getCourse().getInstructor())
                .collect(Collectors.toSet());

        List<CartInstructorCoupon> couponsToRemove = cart.getInstructorCoupons().stream()
                .filter(cic -> !remainingInstructors.contains(cic.getInstructor()))
                .collect(Collectors.toList());

        for (CartInstructorCoupon cic : couponsToRemove) {
            cart.removeInstructorCoupon(cic);
        }

        save(cart);

        response.put("success", true);
        response.put("message", "Thanh toán thành công! Khóa học đã được thêm vào Việc Học Của Tôi.");
        return response;
    }
}
