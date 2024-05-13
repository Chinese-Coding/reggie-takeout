package cn.bupt.edu.zfq.reggietakeout.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SetMeal {
    private Long id;

    private Long categoryId;  // 分类id

    private String name; // 套餐名称

    private BigDecimal price; // 套餐价格

    private String code; // 编码

    private String image; // 图片

    private String description; // 描述信息

    private Integer status; // 状态 0:停用 1:启用

    private Integer sort;

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
