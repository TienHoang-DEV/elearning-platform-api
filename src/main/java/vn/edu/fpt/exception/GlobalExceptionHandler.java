package vn.edu.fpt.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private final ViewResolver viewResolver;

    public GlobalExceptionHandler(ViewResolver viewResolver) {
        this.viewResolver = viewResolver;
    }

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
        return "templates/error/404.html";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) throws Exception {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }
        return "templates/error/404.html";
    }

    @ExceptionHandler(BadRequestException.class)
    public Object handleBadRequest(BadRequestException ex, HttpServletRequest request) throws Exception {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
        return "templates/error/400.html";
    }

    @ExceptionHandler(ApplicationException.class)
    public Object handleApplication(ApplicationException ex, HttpServletRequest request) throws Exception {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Application Error", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return "templates/error/500.html";
    }

    @ExceptionHandler(Exception.class)
    public Object handleOther(Exception ex, HttpServletRequest request) throws Exception {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", ex.getMessage(), request.getRequestURI());
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return "templates/error/500.html";
    }

  
}

