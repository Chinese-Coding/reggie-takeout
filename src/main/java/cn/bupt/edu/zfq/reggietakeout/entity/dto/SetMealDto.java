package cn.bupt.edu.zfq.reggietakeout.entity.dto;

import cn.bupt.edu.zfq.reggietakeout.entity.SetMeal;
import cn.bupt.edu.zfq.reggietakeout.entity.SetMealDish;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SetMealDto extends SetMeal {
    private List<SetMealDish> setMealDishes;

    private String categoryName;
}
