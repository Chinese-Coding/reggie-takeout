package cn.bupt.edu.zfq.reggietakeout.service;

import cn.bupt.edu.zfq.reggietakeout.common.AssociationDeletionException;
import cn.bupt.edu.zfq.reggietakeout.entity.SetMeal;
import cn.bupt.edu.zfq.reggietakeout.entity.SetMealDish;
import cn.bupt.edu.zfq.reggietakeout.entity.dto.SetMealDto;
import cn.bupt.edu.zfq.reggietakeout.mapper.SetMealMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetMealService extends ServiceImpl<SetMealMapper, SetMeal> {
    @Autowired
    private SetMealDishService setMealDishService;

    public void saveWithDish(SetMealDto setMealDto) {
        // 保存套餐基本信息, 操作 setMeal, 执行 insert 操作
        this.save(setMealDto);

        // 保存套餐和菜品的关联信息, 操作 setMealDish, 执行 insert 操作
        var setmealDishes = setMealDto.getSetMealDishes();
        setmealDishes = setmealDishes.stream().peek((item) -> item.setSetMealId(setMealDto.getId())).toList();
        setMealDishService.saveBatch(setmealDishes);
    }

    public void removeWithDish(List<Long> ids) {
        // 查询套餐状态, 确认是否可以删除 (只有那些已经停售的套餐才可以删除)
        var lqw = new LambdaQueryWrapper<SetMeal>();
        lqw.in(SetMeal::getId, ids);
        lqw.eq(SetMeal::getStatus, 1);
        if (this.count(lqw) > 0)
            throw new AssociationDeletionException("套餐正在售卖中, 不能删除");

        this.removeByIds(ids); // 批量删除表 SetMeal 中的数据

        var queryWrapper2 = new LambdaQueryWrapper<SetMealDish>();
        queryWrapper2.in(SetMealDish::getSetMealId, ids);
        setMealDishService.remove(queryWrapper2);
    }

    public SetMealDto getSetMealWithDish(Long id) {
        var setMeal = this.getById(id);
        var setMealDto = new SetMealDto();

        var lqw = new LambdaQueryWrapper<SetMealDish>();
        lqw.eq(id != null, SetMealDish::getSetMealId, id);

        if (setMeal != null) {
            BeanUtils.copyProperties(setMeal, setMealDto);
            var dishes = setMealDishService.list(lqw);
            setMealDto.setSetMealDishes(dishes);
            return setMealDto;
        }

        return null;
    }

    public void updateWithDish(SetMealDto setMealDto) {
        this.updateById(setMealDto);
        var setMealDishes = setMealDto.getSetMealDishes();
        setMealDishes = setMealDishes.stream().peek((item) -> item.setSetMealId(setMealDto.getId())).toList();
        setMealDishService.saveOrUpdateBatch(setMealDishes);
    }
}
