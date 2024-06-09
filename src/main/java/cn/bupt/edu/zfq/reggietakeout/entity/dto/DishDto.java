package cn.bupt.edu.zfq.reggietakeout.entity.dto;

import cn.bupt.edu.zfq.reggietakeout.entity.Dish;
import cn.bupt.edu.zfq.reggietakeout.entity.DishFlavor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
public class DishDto extends Dish {
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
