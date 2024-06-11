package cn.bupt.edu.zfq.reggietakeout.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.apache.ibatis.type.ArrayTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车
 */
@Data
public class ShoppingCart {
    private Long id;

    private String name;

    private Long userId;

    private Long dishId;

    private Long setMealId;

    @TableField(value = "dish_flavor", jdbcType = JdbcType.ARRAY, typeHandler = ArrayTypeHandler.class)
    private String[] dishFlavor;

    private Integer number;

    private BigDecimal amount;

    private String image;

    private LocalDateTime createTime;
}
