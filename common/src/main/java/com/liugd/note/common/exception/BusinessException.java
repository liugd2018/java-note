package com.liugd.note.common.exception;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
public class BusinessException extends RuntimeException{


    private int code;

    private String message;

    public BusinessException(int code, String message) {
        super(String.format("BusinessException: code:%s, message:%s", code, message));
        this.code = code;
        this.message = message;
    }

    public BusinessException(int code, String message, Throwable e) {
        super(String.format("BusinessException: code:%s, message:%s", code, message), e);
        this.code = code;
        this.message = message;
    }

    public int getErrorCode() {
        return code;
    }

    public String getMsg() {
        return message;
    }

}
