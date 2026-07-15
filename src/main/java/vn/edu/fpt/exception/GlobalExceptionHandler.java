package vn.edu.fpt.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return (accept != null && accept.contains("application/json")) || request.getRequestURI().startsWith("/api/");
    }

    private ModelAndView buildErrorView(String viewName, HttpStatus status, String error, String message,
                                        HttpServletRequest request, Exception ex) {
        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject("status", status.value());
        mav.addObject("error", error);
        mav.addObject("message", message);
        mav.addObject("path", request.getRequestURI());
        mav.addObject("timestamp", LocalDateTime.now());

        if (ex != null) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            mav.addObject("trace", sw.toString());
        }
        return mav;
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public Object handleCourseNotFound(CourseNotFoundException ex, HttpServletRequest request) throws Exception {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Course Not Found", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }
        return buildErrorView("error/404", HttpStatus.NOT_FOUND, "Không tìm thấy khóa học", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Forbidden", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
        }

        return buildErrorView("error/403", HttpStatus.FORBIDDEN, "Truy cập bị từ chối", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }
        return buildErrorView("error/404", HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(BadRequestException.class)
    public Object handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
        return buildErrorView("error/400", HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }
        return buildErrorView("error/404", HttpStatus.NOT_FOUND, "Không tìm thấy trang", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(Exception.class)
    public Object handleOther(Exception ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return buildErrorView("error/500", HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra sự cố hệ thống", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(PaymentCallApiException.class)
    public Object handlePaymetApiCallFailException(PaymentCallApiException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.BAD_GATEWAY.value(),  "Bad Gateway", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.BAD_GATEWAY);
        }
        return buildErrorView("error/500", HttpStatus.BAD_GATEWAY, "Lỗi kết nối thanh toán", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(PaymentCreateException.class)
    public Object handlePaymentCreateException(PaymentCreateException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),  "Create Payment Fail", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return buildErrorView("error/500", HttpStatus.INTERNAL_SERVER_ERROR, "Tạo thanh toán thất bại", ex.getMessage(), request, ex);
    }

}
