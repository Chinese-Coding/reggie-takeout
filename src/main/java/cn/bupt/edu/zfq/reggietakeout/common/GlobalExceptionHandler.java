package cn.bupt.edu.zfq.reggietakeout.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

/**
 * 全局异常处理
 */
@Slf4j
@ResponseBody
@ControllerAdvice(annotations = {RestController.class, Controller.class})
public class GlobalExceptionHandler {
    /**
     * `DuplicateKeyException`异常处理方法, 根据所使用的 PostgreSQL 进行了修改
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public R<String> exceptionHandler(DuplicateKeyException e) {
        var message = e.getMessage();
        log.error(e.getMessage());
        var pattern = Pattern.compile("键值\"\\((.*?)\\)=\\((.*?)\\)\" 已经存在");
        var matcher = pattern.matcher(message);
        if (matcher.find()) {
            String fieldName = matcher.group(1), fieldValue = matcher.group(2);
            return R.error("错误：字段 '" + fieldName + "' 的值 '" + fieldValue + "' 已存在.");
        }
        return R.error("未知错误");
    }

    /**
     * `AssociationDeletionException`异常处理方法
     */
    @ExceptionHandler(AssociationDeletionException.class)
    public R<String> exceptionHandler(AssociationDeletionException e) {
        var message = e.getMessage();
        log.error(message);
        return R.error(message);
    }
}
