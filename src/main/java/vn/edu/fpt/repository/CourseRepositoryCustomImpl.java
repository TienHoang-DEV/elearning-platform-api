package vn.edu.fpt.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.dto.course.CourseListDto;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CourseRepositoryCustomImpl implements CourseRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<CourseListDto> getPagedCoursesSummary(
            String search,
            Integer categoryId,
            List<Double> ratings,
            List<String> prices,
            String sort,
            int page,
            int size) {

        StringBuilder whereClause = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();

        // 1. Lọc theo search (tiêu đề khóa học, không phân biệt hoa thường)
        if (search != null && !search.trim().isEmpty()) {
            whereClause.append(" AND LOWER(c.title) LIKE :search");
            parameters.put("search", "%" + search.trim().toLowerCase() + "%");
        }

        // 2. Lọc theo categoryId
        if (categoryId != null) {
            whereClause.append(" AND cat.id = :categoryId");
            parameters.put("categoryId", categoryId);
        }

        // 3. Lọc theo ratings (lấy giá trị min trong các mức rating được chọn)
        if (ratings != null && !ratings.isEmpty()) {
            double minRating = Double.MAX_VALUE;
            for (Double r : ratings) {
                if (r != null && r < minRating) {
                    minRating = r;
                }
            }
            if (minRating != Double.MAX_VALUE) {
                whereClause.append(" AND (SELECT COALESCE(AVG(f.rating), 0.0) FROM Feedback f WHERE f.course.id = c.id) >= :minRating");
                parameters.put("minRating", minRating);
            }
        }

        // 4. Lọc theo khoảng giá (Prices) - Kết hợp nhiều khoảng bằng OR
        if (prices != null && !prices.isEmpty()) {
            StringBuilder priceClause = new StringBuilder(" AND (");
            boolean first = true;
            for (int i = 0; i < prices.size(); i++) {
                String[] parts = prices.get(i).split("-");
                if (parts.length == 2) {
                    try {
                        double min = Double.parseDouble(parts[0]);
                        double max = Double.parseDouble(parts[1]);
                        if (!first) {
                            priceClause.append(" OR ");
                        }
                        priceClause.append("c.price BETWEEN :minPrice").append(i).append(" AND :maxPrice").append(i);
                        parameters.put("minPrice" + i, BigDecimal.valueOf(min));
                        parameters.put("maxPrice" + i, BigDecimal.valueOf(max));
                        first = false;
                    } catch (NumberFormatException ignored) {}
                }
            }
            priceClause.append(")");
            if (!first) { // Có ít nhất một khoảng giá hợp lệ được append
                whereClause.append(priceClause);
            }
        }

        // 5. Sắp xếp (Sort)
        StringBuilder orderClause = new StringBuilder();
        if ("rating".equals(sort)) {
            orderClause.append(" ORDER BY (SELECT COALESCE(AVG(f.rating), 0.0) FROM Feedback f WHERE f.course.id = c.id) DESC");
        } else if ("price-asc".equals(sort)) {
            orderClause.append(" ORDER BY c.price ASC");
        } else if ("price-desc".equals(sort)) {
            orderClause.append(" ORDER BY c.price DESC");
        } else {
            orderClause.append(" ORDER BY c.id DESC");
        }

        // Xây dựng câu truy vấn count
        String countJpql = "SELECT COUNT(c.id) FROM Course c JOIN c.instructor i JOIN c.category cat WHERE c.status = vn.edu.fpt.enums.CourseStatus.PUBLISHED" + whereClause.toString();
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalElements = countQuery.getSingleResult();

        // Ràng buộc trang hợp lệ để tính offset chính xác
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages < 1) {
            totalPages = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }
        if (page < 1) {
            page = 1;
        }

        // Xây dựng câu truy vấn select lấy DTO
        String selectJpql = "SELECT new vn.edu.fpt.dto.course.CourseListDto(c.id, c.title, c.thumbnailUrl, c.price, c.level, i.firstName, i.lastName, i.id, cat.id, cat.name, " +
                "(SELECT COALESCE(AVG(f.rating), 0.0) FROM Feedback f WHERE f.course.id = c.id), " +
                "(SELECT COUNT(f.id) FROM Feedback f WHERE f.course.id = c.id), " +
                "(SELECT COUNT(l.id) FROM CourseSection cs JOIN cs.lessons l WHERE cs.course.id = c.id), " +
                "(SELECT COUNT(e.id) FROM Enrollment e WHERE e.course.id = c.id)) " +
                "FROM Course c JOIN c.instructor i JOIN c.category cat WHERE c.status = vn.edu.fpt.enums.CourseStatus.PUBLISHED" + 
                whereClause.toString() + orderClause.toString();

        TypedQuery<CourseListDto> query = entityManager.createQuery(selectJpql, CourseListDto.class);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        int firstResult = (page - 1) * size;
        query.setFirstResult(firstResult);
        query.setMaxResults(size);

        List<CourseListDto> content = query.getResultList();
        Pageable pageable = PageRequest.of(page - 1, size);

        return new PageImpl<>(content, pageable, totalElements);
    }
}
