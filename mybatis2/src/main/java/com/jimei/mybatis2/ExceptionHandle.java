package com.jimei.mybatis2;

import com.google.gson.Gson;
import com.youpin.common.base.BusiException;
import com.youpin.common.base.OutDTO;
import com.youpin.common.consts.EStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yudm
 * @date 2020/2/24 22:06
 * @description 统一异常处理
 */
@Slf4j
@RestControllerAdvice
public class ExceptionHandle {
    private final Gson gson = new Gson();

    //用注解进行数据校验方面的异常

    /**
     * 对方法参数校验异常处理方法(仅对于表单提交有效，对于以json格式提交将会失效)
     * 如果是表单类型的提交，则spring会采用表单数据的处理类进行处理（进行参数校验错误时会抛出BindException异常）
     */
    @ExceptionHandler(BindException.class)
    public OutDTO<String> handle(BindException e) {
        log.error(getDetailMessage(e));
        return parseExceptionMsg(e);
    }

    /**
     * 对方法参数校验异常处理方法(前端提交的方式为json格式出现异常时会被该异常类处理)
     * json格式提交时，spring会采用json数据的数据转换器进行处理（进行参数校验时错误是抛出MethodArgumentNotValidException异常）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public OutDTO<String> handle(MethodArgumentNotValidException e) {
        log.error(getDetailMessage(e));
        return parseExceptionMsg(e);
    }

    private OutDTO<String> parseExceptionMsg(Exception e) {
        BindingResult result;
        //将异常进行强转
        if (e instanceof BindException) {
            BindException exception = (BindException) e;
            result = exception.getBindingResult();
        } else {
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) e;
            result = exception.getBindingResult();
        }

        //解析出自定义的异常信息
        if (result.hasErrors()) {
            List<String> msgList = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
            return OutDTO.fail(EStatus.NOT_VALID.getCode(), msgList.toString());
        }
        return OutDTO.fail(EStatus.NOT_VALID.getCode(), EStatus.NOT_VALID.getMsg());
    }

    //业务异常处理方法
    @ExceptionHandler(value = BusiException.class)
    public OutDTO<String> handle(BusiException b) {
        log.error(getDetailMessage(b));
        return OutDTO.fail(b.getCode(), b.getMessage() + " at " + b.getStackTrace()[0]);
    }

    //操作数据库的异常处理方法，需要展示纤细信息
    @ExceptionHandler(value = SQLException.class)
    public OutDTO<String> handle(SQLException s) {
        log.error(getDetailMessage(s));
        return OutDTO.fail(s.getMessage() + " at " + s.getStackTrace()[0]);
    }


    //其它异常处理方法
    @ExceptionHandler(value = Exception.class)
    public Result handle(Exception e) {
        log.error(getDetailMessage(e));
        return Result.faild(e.getClass().getName(), gson.toJson(e));
    }

    //其它异常处理方法
    @ExceptionHandler(value = Throwable.class)
    public Result handle(Throwable t) {
        return Result.faild(t.getClass().getName(), gson.toJson(t));
    }

    //获取异常的详细信息
    private static String getDetailMessage(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        StringBuffer buffer = stringWriter.getBuffer();
        return buffer.toString();
    }

}
