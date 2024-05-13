package cn.bupt.edu.zfq.reggietakeout.entity.dto;

import cn.bupt.edu.zfq.reggietakeout.entity.SetMeal;
import cn.bupt.edu.zfq.reggietakeout.entity.SetMealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetMealDto extends SetMeal {
    private List<SetMealDish> setMealDishes;

    private String categoryName;
}
