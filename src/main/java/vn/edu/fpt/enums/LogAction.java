package vn.edu.fpt.enums;

public enum LogAction {
    // Authentication & Security
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    CHANGE_PASSWORD,
    RESET_PASSWORD_REQUEST,

    // Payment & Transactions
    CREATE_PAYMENT,
    PAYMENT_WEBHOOK_RECEIVED,
    PAYMENT_COMPLETED,
    CANCEL_PAYMENT,
    EXPIRE_PAYMENT,

    // Approvals
    APPROVE_INSTRUCTOR,
    REJECT_INSTRUCTOR,
    APPROVE_COURSE,
    REJECT_COURSE,

    // Course Management
    CREATE_COURSE,
    UPDATE_COURSE_PRICE,
    DELETE_COURSE,
    CREATE_LESSON,
    DELETE_LESSON,

    // Admin Operations
    BLOCK_USER,
    UNBLOCK_USER,
    UPDATE_SYSTEM_CONFIG
}
