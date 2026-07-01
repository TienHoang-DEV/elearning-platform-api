package vn.edu.fpt.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.dto.revenueInstructor.CourseRevenueDto;
import vn.edu.fpt.entity.Order;
import vn.edu.fpt.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUser(User user);

    ///Tổng danh thu cho cho mỗi giảng vien
    @Query("""
          select COALESCE(sum(oi.priceSnapshot), 0) from OrderItem oi 
          where oi.course.instrcutor.id = :instructorId 
          and oi.order.status = 'COMPLETED'
          and oi.order.createdAt between :fromDate and :toDate
          """)
    BigDecimal sumTotalRevenueByInstructor(@Param("instructorId") Integer instructorId,
                                           @Param("fromDate")LocalDateTime fromDate,
                                           @Param("toDate") LocalDateTime toDate);

    ///Tổng số lượng Order
    @Query("""
          select count(distinct oi.order.id) from OrderItem oi 
          where oi.course.instructor.id = :instructorId
          and oi.order.status = 'COMPLETED'
          and oi.order.createdAt between :fromDate and :toDate
          """)
    Integer countOrder(@Param("instructorId") Integer instructorId,
                       @Param("fromDate") LocalDateTime fromDate,
                       @Param("toDate") LocalDateTime toDate);


    //Top các khoá hoc
    @Query("""
         select new vn.edu.fpt.dto.revenueInstructor.CourseRevenueDto(oi.course.title, SUM(oi.priceSnapshot))
         from OrderItem oi
         where oi.order.instructor.id = :instructorId
         and oi.order.status = 'Completed'
         and oi.order.createdAt between :fromDate and :toDate
        group by oi.course.title order by sum(oi.priceSnapshot) desc 
         """)
    List<CourseRevenueDto> topSellingCourse(@Param("instructorId") Integer instructorId,
                                            @Param("fromDate") LocalDateTime fromDate,
                                            @Param("toDate") LocalDateTime toDate);


}

