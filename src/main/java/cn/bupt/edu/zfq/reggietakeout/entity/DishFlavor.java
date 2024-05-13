package cn.bupt.edu.zfq.reggietakeout.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.ArrayTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

@Data
@TableName(autoResultMap = true)
public class DishFlavor {
    private Long id;

    private Long dishId; // 菜品id

    private String name; // 口味名称

    @TableField(value = "value", jdbcType = JdbcType.ARRAY, typeHandler = ArrayTypeHandler.class)
    private String[] value; // 口味数据数组

    private Boolean deleted; // 是否删除

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
