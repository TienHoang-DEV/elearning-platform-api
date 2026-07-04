package vn.edu.fpt.repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.dto.revenueInstructor.CoursePerformanceDto;
import vn.edu.fpt.dto.revenueInstructor.CourseRevenueDto;
import vn.edu.fpt.dto.revenueInstructor.RecentOrderDto;
import vn.edu.fpt.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    ///Tổng danh thu cho cho mỗi giảng vien
    @Query("""
          select COALESCE(sum(oi.priceSnapshot), 0) from OrderItem oi 
          where oi.course.instructor.id = :instructorId 
          and oi.order.status = 'COMPLETED'
          and oi.order.createdAt between :fromDate and :toDate
          """)
    BigDecimal sumTotalRevenueByInstructor(@Param("instructorId") Integer instructorId,
                                           @Param("fromDate") LocalDateTime fromDate,
                                           @Param("toDate") LocalDateTime toDate);

    ///Tổng số lượng Order
    @Query("""
          select count(distinct oi.order.id) from OrderItem oi 
          where oi.course.instructor.id = :instructorId
          and oi.order.status = 'COMPLETED'
          and oi.order.createdAt between :fromDate and :toDate
          """)
    Long countOrder(@Param("instructorId") Integer instructorId,
                    @Param("fromDate") LocalDateTime fromDate,
                    @Param("toDate") LocalDateTime toDate);


    //Top các khoá học - top 4
    @Query("""
         select new vn.edu.fpt.dto.revenueInstructor.CourseRevenueDto(oi.course.title, SUM(oi.priceSnapshot))
         from OrderItem oi
         where oi.course.instructor.id = :instructorId
         and oi.order.status = 'COMPLETED'
         and oi.order.createdAt between :fromDate and :toDate
        group by oi.course.title order by sum(oi.priceSnapshot) desc 
         """)
    List<CourseRevenueDto> topSellingCourse(@Param("instructorId") Integer instructorId,
                                            @Param("fromDate") LocalDateTime fromDate,
                                            @Param("toDate") LocalDateTime toDate,
                                            Pageable pagebale);

    //Hiệu suất số lượng chốt khoá học thành công
    @Query("""
          select new vn.edu.fpt.dto.revenueInstructor.CoursePerformanceDto(oi.course.title, count(oi))
          from OrderItem oi
          where oi.course.instructor.id = :instructorId
          and oi.order.status = 'COMPLETED'
          and oi.order.createdAt between :fromDate and :toDate
          group by oi.course.title order by count(oi) desc
          """)
    List<CoursePerformanceDto> coursePerformance(@Param("instructorId") Integer instructorId,
                                                 @Param("fromDate") LocalDateTime fromDate,
                                                 @Param("toDate") LocalDateTime toDate,
                                                 Pageable pageable);

    //Danh sách order gần nhất
    @Query("""
           select new vn.edu.fpt.dto.revenueInstructor.RecentOrderDto(oi.order.user.firstName, oi.order.user.lastName, oi.course.title, oi.priceSnapshot, oi.order.createdAt)
           from OrderItem oi
           where oi.course.instructor.id = :instructorId
           and oi.order.status = 'COMPLETED'
           order by oi.order.createdAt desc
           """)
    List<RecentOrderDto> recentOrder(@Param("instructorId") Integer instructorId,
                                     Pageable pageable);

    //Doanh thu theo ngày cho biểu đồ
    @Query(value = "select convert(DATE, o.created_at) as d, sum(oi.price_snapshot) as rev \n" +
            "          from order_items oi \n" +
            "          join orders o on oi.order_id = o.id\n" +
            "          join courses c on c.id = oi.course_id\n" +
            "          where c.instructor_id = :instructorId and o.status = 'COMPLETED' and o.created_at between :fromDate and :toDate\n" +
            "          group by convert(DATE, o.created_at) order by d", nativeQuery = true)
    List<Object[]> revenueTrend(@Param("instructorId") Integer instructorId,
                                @Param("fromDate") LocalDateTime fromDate,
                                @Param("toDate") LocalDateTime toDate);
}
