package cn.bupt.edu.zfq.reggietakeout.entity;

import lombok.Data;

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

    private String[] dishFlavor;

    private Integer number;

    private BigDecimal amount;

    private String image;

    private LocalDateTime createTime;
}
