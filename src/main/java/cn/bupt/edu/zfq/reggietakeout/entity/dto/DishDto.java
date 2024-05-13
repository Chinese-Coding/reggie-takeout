package cn.bupt.edu.zfq.reggietakeout.entity.dto;

import cn.bupt.edu.zfq.reggietakeout.entity.Dish;
import cn.bupt.edu.zfq.reggietakeout.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
