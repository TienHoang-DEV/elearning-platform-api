package vn.edu.fpt.controller;


import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.dto.CouponDto;
import vn.edu.fpt.dto.ProfileDto;
import vn.edu.fpt.dto.UserDto;
import vn.edu.fpt.entity.Coupon;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.CouponStatus;
import vn.edu.fpt.enums.DiscountType;
import vn.edu.fpt.exception.UserValidationException;
import vn.edu.fpt.security.CustomUserDetails;
import vn.edu.fpt.service.CategoryService;
import vn.edu.fpt.service.CouponService;
import vn.edu.fpt.service.UserService;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.util.SecurityUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/instructor")
@Controller
public class InstructorProfileController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final CouponService couponService;

    public InstructorProfileController(UserService userService, CategoryService categoryService, CouponService couponService) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.couponService = couponService;
    }

    @GetMapping("/sidebar")
    public String Sidebar(Model model) {
        User user = SecurityUtils.getCurrentUser();
        ProfileDto profileDto = new ProfileDto();
        profileDto.setFirstname(user.getFirstName());
        profileDto.setLastname(user.getLastName());
        profileDto.setBio(user.getBio());
        profileDto.setAvatar_url(user.getAvatarUrl());
        profileDto.setEmail(user.getEmail());
        profileDto.setPhone(user.getPhone());
        model.addAttribute("instructor", profileDto);
        return "instructor_course/profile";
    }


    @PostMapping("/profiles")
    public String updateProfile(
            @Valid
            @ModelAttribute("instructor") ProfileDto profileDto,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "instructor_course/profile";
        }
        try {

            User tmp = SecurityUtils.getCurrentUser();
            userService.updateProfileInstuctor(tmp, profileDto);
            redirectAttributes.addFlashAttribute("success", "Thay đổi thành công!!!");
            return "redirect:/instructor/sidebar";
        } catch (UserValidationException e) {
            result.rejectValue(e.getFeild(), "error", e.getMessage());
            return "instructor_course/profile";
        }

    }

    @GetMapping("/coupon")
    public String createCouponScreen(Model model) {
        User currentUser = SecurityUtils.getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("statuses", CouponStatus.values());
        model.addAttribute("discountTypes", DiscountType.values());
        model.addAttribute("coupon", new CouponDto());
        return "coupon/coupon-create";
    }

    @PostMapping("/coupon")
    public String createCoupon(@ModelAttribute("coupon") CouponDto request,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (result.hasFieldErrors("discountValue")) {
            result.rejectValue(
                    "discountValue",
                    "discountValue.invalid",
                    "Giá trị giảm giá phải là số");
        }

        if (result.hasFieldErrors("usageLimit")) {
            result.rejectValue(
                    "usageLimit",
                    "usageLimit.invalid",
                    "Số lượt sử dụng tối đa phải là số nguyên");
        }


        if (request.getTitle() == null
                || request.getTitle().trim().isEmpty()
                || request.getTitle().length() > 255) {

            result.rejectValue(
                    "title",
                    "title.invalid",
                    "Tên chiến dịch phải từ 1 đến 255 ký tự");
        }

        if (request.getDiscountType() == null) {

            result.rejectValue(
                    "discountType",
                    "discountType.required",
                    "Vui lòng chọn loại chiết khấu");
        }

        if (request.getStatus() == null) {

            result.rejectValue(
                    "status",
                    "status.required",
                    "Vui lòng chọn trạng thái");
        }

        if (!result.hasFieldErrors("discountValue")
                && request.getDiscountType() == DiscountType.PERCENT) {

            if (request.getDiscountValue() == null
                    || request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0
                    || request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {

                result.rejectValue(
                        "discountValue",
                        "discountValue.invalid",
                        "Giá trị phần trăm phải lớn hơn 0 và tối đa là 100");
            }
        }

        if (!result.hasFieldErrors("discountValue")
                && request.getDiscountType() == DiscountType.FIXED) {

            if (request.getDiscountValue() == null
                    || request.getDiscountValue().compareTo(BigDecimal.ONE) < 0) {

                result.rejectValue(
                        "discountValue",
                        "discountValue.invalid",
                        "Giá trị giảm phải lớn hơn 0");
            }
        }

        if (request.getUsageLimit() != null
                && request.getUsageLimit() <= 0) {

            result.rejectValue(
                    "usageLimit",
                    "usageLimit.invalid",
                    "Số lượt sử dụng tối đa nếu được nhập phải lớn hơn 0");
        }

        if (result.hasErrors()) {
            System.out.println("ExpiredAt = " + request.getExpiredAt());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("statuses", CouponStatus.values());
            model.addAttribute("discountTypes", DiscountType.values());
            return "coupon/coupon-create";
        }

        couponService.createCoupon(request);

        redirectAttributes.addFlashAttribute("successMessage", "Phát hành mã giảm giá mới thành công!");

        return "redirect:/instructor/coupon";
    }

}
