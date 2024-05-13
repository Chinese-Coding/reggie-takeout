package cn.bupt.edu.zfq.reggietakeout.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品
 */
@Data
public class Dish {
    private Long id;

    private String name;  // 菜品名称

    private Long categoryId;     // 菜品分类 id

    private BigDecimal price;     // 菜品价格

    private String code;     // 商品码

    private String image;     // 图片

    private String description;     // 描述信息

    private Integer status; // 0 停售 1 起售

    private Integer sort;    // 顺序

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
