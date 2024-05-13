package cn.bupt.edu.zfq.reggietakeout.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义元数据对象处理器, 实现数据库某些项的自动填写
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Autowired
    private HttpSession session;

    /**
     * 插入操作自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充 [insert]...");
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        var employeeId = (Long) session.getAttribute("employee");
        if (employeeId != null) {
            metaObject.setValue("createUser", employeeId);
            metaObject.setValue("updateUser", employeeId);
        }
    }

    /**
     * 更新操作自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充 [update]...");
        metaObject.setValue("updateTime", LocalDateTime.now());
        var employeeId = (Long) session.getAttribute("employee");
        if (employeeId != null)
            metaObject.setValue("updateUser", employeeId);
    }
}
