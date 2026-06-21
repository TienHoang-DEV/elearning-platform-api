package vn.edu.fpt.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.entity.Coupon;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.service.CouponService;
import vn.edu.fpt.util.SecurityUtils;

import java.util.List;

@Controller
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/list-coupon")
    public String listCoupon(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size){

        User currentUser = SecurityUtils.getCurrentUser();
        model.addAttribute("currentUser", currentUser);

        // 2. Gọi Service lấy dữ liệu phân trang từ Database
        // Hàm findAll(Pageable) của Spring Data JPA sẽ trả về đối tượng Page<Coupon>
        Page<Coupon> couponPage = couponService.findAll(PageRequest.of(page, size));

        // 3. KHAI BÁO VÀ ĐẨY CÁC BIẾN SANG THYMELEAF
        model.addAttribute("listCoupon", couponPage.getContent()); // Danh sách coupon của trang hiện tại
        model.addAttribute("currentPage", couponPage.getNumber());  // Trang hiện tại (chính là biến bạn hỏi)
        model.addAttribute("totalPages", couponPage.getTotalPages()); // Tổng số trang
        model.addAttribute("totalElements", couponPage.getTotalElements()); // Tổng số bản ghi trong DB
        model.addAttribute("pageSize", couponPage.getSize());        // Số lượng phần tử trên 1 trang

        return "coupon/coupon-list";
    }
}
