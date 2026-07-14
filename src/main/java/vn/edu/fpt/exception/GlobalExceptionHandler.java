package vn.edu.fpt.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.entity.User;

/**
 * Global handler to convert exceptions into consistent HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return (accept != null && accept.contains("application/json")) || request.getRequestURI().startsWith("/api/");
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public Object handleCourseNotFound(CourseNotFoundException ex, HttpServletRequest request) throws Exception {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Course Not Found", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }
        return "error/404";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Forbidden", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
        }
        return "error/403";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }
        return "error/404";
    }

    @ExceptionHandler(BadRequestException.class)
    public Object handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
        return "error/400";
    }

    @ExceptionHandler(Exception.class)
    public Object handleOther(Exception ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return "error/500";
    }

    @ExceptionHandler(PaymentCallApiException.class)
    public Object handlePaymetApiCallFailException(Exception ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.BAD_GATEWAY.value(),  "Bad Gateway", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.BAD_GATEWAY);
        }
        return "error/500";
    }

    @ExceptionHandler(PaymentCreateException.class)
    public Object handlePaymentCreateException(Exception ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),  "Create Payment Fail", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return "error/500";
    }

  
}

