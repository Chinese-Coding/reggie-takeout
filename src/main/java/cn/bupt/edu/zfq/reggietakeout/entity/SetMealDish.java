package cn.bupt.edu.zfq.reggietakeout.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 套餐菜品关系
 */
@Data
public class SetMealDish {
    private Long id;

    private Long setMealId; // 套餐id

    private Long dishId; // 菜品id

    private String name; // 菜品名称 (冗余字段)

    private BigDecimal price; // 菜品原价 (冗余字段)

    private Integer copies; // 份数

    private Integer sort; // 排序

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
