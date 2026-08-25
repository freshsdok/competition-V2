package com.teaching.common.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.teaching.common.core.constant.HttpStatus;
import com.teaching.common.core.exception.DemoModeException;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.exception.InnerAuthException;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.exception.auth.NotPermissionException;
import com.teaching.common.core.exception.auth.NotRoleException;
import com.teaching.common.core.text.Convert;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.html.EscapeUtil;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.ClientInfoUtils;
import com.teaching.common.core.utils.ServletUtils;
import com.teaching.common.core.utils.ip.IpUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteErrorLogService;
import com.teaching.system.api.domain.SysErrorLog;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.UUID;
import org.springframework.scheduling.annotation.Async;

/**
 * 全局异常处理器
 *
 * @author teaching
 */
@RestControllerAdvice
public class GlobalExceptionHandler
{
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired(required = false)
    private RemoteErrorLogService remoteErrorLogService;


    /**
     * 处理客户端提前断开连接的异常
     * 这不是真正的错误，只是客户端行为，记录为 DEBUG 级别即可
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsableException(AsyncRequestNotUsableException ex) {
        // 不需要返回任何内容，因为客户端已经断开
        log.debug("Client disconnected before response completed: {}", ex.getMessage());
    }

    /**
     * 权限码异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public AjaxResult handleNotPermissionException(NotPermissionException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',权限码校验失败'{}'", requestURI, e.getMessage());
        return AjaxResult.error(HttpStatus.FORBIDDEN, "没有访问权限，请联系管理员授权");
    }

    /**
     * 角色权限异常
     */
    @ExceptionHandler(NotRoleException.class)
    public AjaxResult handleNotRoleException(NotRoleException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',角色权限校验失败'{}'", requestURI, e.getMessage());
        return AjaxResult.error(HttpStatus.FORBIDDEN, "没有访问权限，请联系管理员授权");
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e, HttpServletRequest request)
    {
        log.error(e.getMessage(), e);
        Integer code = e.getCode();
        return StringUtils.isNotNull(code) ? AjaxResult.error(code, e.getMessage()) : AjaxResult.error(e.getMessage());
    }

    /**
     * 可直接展示给用户的业务校验异常。
     */
    @ExceptionHandler(GlobalException.class)
    public AjaxResult handleGlobalException(GlobalException e, HttpServletRequest request)
    {
        log.warn("请求地址'{}',业务校验未通过'{}'", request.getRequestURI(), e.getMessage());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 请求路径中缺少必需的路径变量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public AjaxResult handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求路径中缺少必需的路径变量'{}',发生系统异常.", requestURI, e);
        return AjaxResult.error(String.format("请求路径中缺少必需的路径变量[%s]", e.getVariableName()));
    }

    /**
     * 请求参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public AjaxResult handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        String value = Convert.toStr(e.getValue());
        if (StringUtils.isNotEmpty(value))
        {
            value = EscapeUtil.clean(value);
        }
        log.error("请求参数类型不匹配'{}',发生系统异常.", requestURI, e);
        return AjaxResult.error(String.format("请求参数类型不匹配，参数[%s]要求类型为：'%s'，但输入值为：'%s'", e.getName(), e.getRequiredType().getName(), value));
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        String errorId = createErrorId();
        log.error("请求地址'{}',发生未知异常，errorId={}.", requestURI, errorId, e);
        // 记录错误日志
        recordErrorLog(e, request, errorId);
        return safeSystemError(errorId);
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        String errorId = createErrorId();
        log.error("请求地址'{}',发生系统异常，errorId={}.", requestURI, errorId, e);
        // 记录错误日志
        recordErrorLog(e, request, errorId);
        return safeSystemError(errorId);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public AjaxResult handleBindException(BindException e)
    {
        log.error(e.getMessage(), e);
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e)
    {
        log.error(e.getMessage(), e);
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 内部认证异常
     */
    @ExceptionHandler(InnerAuthException.class)
    public AjaxResult handleInnerAuthException(InnerAuthException e)
    {
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 演示模式异常
     */
    @ExceptionHandler(DemoModeException.class)
    public AjaxResult handleDemoModeException(DemoModeException e)
    {
        return AjaxResult.error("演示模式，不允许操作");
    }

    /**
     * 记录错误日志（异步）
     */
    @Async
    protected void recordErrorLog(Exception e, HttpServletRequest request, String errorId)
    {
        try
        {
            if (remoteErrorLogService != null)
            {
                //不入库的错误类型
                if(e instanceof HttpMessageNotReadableException
                        && e.getMessage() != null
                        && e.getMessage().contains("I/O error while reading input message")){
                    return;
                }
                SysErrorLog errorLog = buildErrorLog(e, request, errorId);
                remoteErrorLogService.saveErrorLog(errorLog, com.teaching.common.core.constant.SecurityConstants.INNER);
            }
        }
        catch (Exception ex)
        {
            log.error("记录错误日志失败", ex);
        }
    }

    /**
     * 构建错误日志对象
     */
    private SysErrorLog buildErrorLog(Exception exception, HttpServletRequest request, String errorId)
    {
        SysErrorLog errorLog = new SysErrorLog();

        // 使用与客户端响应一致的错误编码，便于安全审计和问题定位
        errorLog.setErrorCode(errorId);

        // 错误类型判断
        errorLog.setErrorType(getErrorType(exception));

        // 错误级别
        errorLog.setErrorLevel("ERROR");

        // 错误消息
        errorLog.setErrorMessage(StringUtils.substring(exception.getMessage(), 0, 2000));

        // 异常类名
        errorLog.setExceptionClass(exception.getClass().getName());

        // 异常方法（从堆栈中获取）
        StackTraceElement[] stackTrace = exception.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0)
        {
            StackTraceElement element = stackTrace[0];
            errorLog.setExceptionMethod(element.getClassName() + "." + element.getMethodName() + ":" + element.getLineNumber());
        }

        // 堆栈信息
        errorLog.setStackTrace(getStackTrace(exception));

        // 请求信息
        if (request != null)
        {
            errorLog.setRequestUrl(StringUtils.substring(request.getRequestURI(), 0, 500));
            errorLog.setRequestMethod(request.getMethod());

            // 请求参数
            try
            {
                String params = ServletUtils.getParamMap(request).toString();
                errorLog.setRequestParam(StringUtils.substring(params, 0, 2000));
            }
            catch (Exception e)
            {
                errorLog.setRequestParam("");
            }

            // User-Agent
            String userAgent = ClientInfoUtils.getUserAgent(request);
            errorLog.setUserAgent(StringUtils.substring(userAgent, 0, 500));

            // 浏览器和操作系统
            errorLog.setBrowser(ClientInfoUtils.parseBrowser(userAgent));
            errorLog.setOs(ClientInfoUtils.parseOS(userAgent));
        }

        // 操作人员
        try
        {
            errorLog.setOperName(SecurityUtils.getUsername());
        }
        catch (Exception e)
        {
            errorLog.setOperName("匿名用户");
        }

        // 操作IP
        errorLog.setOperIp(IpUtils.getIpAddr());

        // 状态：未处理
        errorLog.setStatus("0");

        // 错误时间
        errorLog.setErrorTime(new Date());

        return errorLog;
    }

    private String createErrorId()
    {
        return "ERR-" + UUID.randomUUID().toString().replace("-", "");
    }

    private AjaxResult safeSystemError(String errorId)
    {
        return AjaxResult.error(HttpStatus.ERROR, "系统繁忙，请稍后重试")
                .put("errorId", errorId);
    }

    /**
     * 获取错误类型
     */
    private String getErrorType(Exception exception)
    {
        String exceptionName = exception.getClass().getName();

        if (exceptionName.contains("SQLException") || exceptionName.contains("DataAccessException"))
        {
            return "SQL错误";
        }
        else if (exceptionName.contains("IOException") || exceptionName.contains("TimeoutException"))
        {
            return "网络错误";
        }
        else if (exception instanceof ServiceException
                || exception instanceof GlobalException
                || exceptionName.contains("BusinessException"))
        {
            return "业务错误";
        }
        else
        {
            return "系统错误";
        }
    }

    /**
     * 获取堆栈信息
     */
    private String getStackTrace(Exception exception)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String stackTrace = sw.toString();

        // 限制堆栈信息长度，避免过大
        if (stackTrace.length() > 10000)
        {
            stackTrace = stackTrace.substring(0, 10000) + "\n... (堆栈信息过长，已截断)";
        }

        return stackTrace;
    }
}
