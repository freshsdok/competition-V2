package com.teaching.common.core.exception;

/**
 * Excel导入验证异常
 *
 * @author teaching
 */
public class ExcelImportRequiredException extends RuntimeException
{
    private static final long serialVersionUID = 111L;

    private Integer code;

    private String message;

    public ExcelImportRequiredException(String message)
    {
        this.message = message;
    }

    public ExcelImportRequiredException(String message, Integer code)
    {
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage()
    {
        return message;
    }

    public Integer getCode()
    {
        return code;
    }

    public ExcelImportRequiredException setMessage(String message)
    {
        this.message = message;
        return this;
    }
}
