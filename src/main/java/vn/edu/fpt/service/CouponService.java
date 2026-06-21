package vn.edu.fpt.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.CouponDto;
import vn.edu.fpt.entity.Coupon;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.CouponStatus;
import vn.edu.fpt.enums.DiscountType;
import vn.edu.fpt.repository.CouponRepository;
import vn.edu.fpt.util.SecurityUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CouponService {
    private final CouponRepository repository;

    public CouponService(CouponRepository couponRepository) {
        this.repository = couponRepository;
    }

    public List<Coupon> findAll() { return repository.findAll(); }
    public Optional<Coupon> findById(Integer id) { return repository.findById(id); }
    public Coupon save(Coupon entity) { return repository.save(entity); }
    public void deleteById(Integer id) { repository.deleteById(id); }
    public boolean existsById(Integer id) { return repository.existsById(id); }
    public Page<Coupon> findAll(Pageable pageable){
        return repository.findAll(pageable);
    }


    public Coupon createCoupon(CouponDto dto) {
        User instructor = SecurityUtils.getCurrentUser();

        Coupon coupon = Coupon.builder()
                .title(dto.getTitle())
                .code(generateCouponCode())
                .discountType(dto.getDiscountType().name())
                .discountValue(dto.getDiscountValue())
                .usageLimit(dto.getUsageLimit())
                .expiredAt(dto.getExpiredAt().plusDays(1).atStartOfDay())
                .status(dto.getStatus().name())
                .instructor(instructor)
                .build();

        return repository.save(coupon);
    }

    private String generateCouponCode() {

        String code;

        do {
            code = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();

        } while (repository.existsByCode(code));

        return code;
    }


}
