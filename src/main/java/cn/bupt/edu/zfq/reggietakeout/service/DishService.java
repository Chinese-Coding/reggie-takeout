package cn.bupt.edu.zfq.reggietakeout.service;

import cn.bupt.edu.zfq.reggietakeout.entity.Dish;
import cn.bupt.edu.zfq.reggietakeout.entity.DishFlavor;
import cn.bupt.edu.zfq.reggietakeout.entity.dto.DishDto;
import cn.bupt.edu.zfq.reggietakeout.mapper.DishMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DishService extends ServiceImpl<DishMapper, Dish> {
    @Autowired
    private DishFlavorService dishFlavorService;

    public void saveWithFlavor(DishDto dishDto) {
        this.save(dishDto); // 保存菜品基本信息到表 dish

        // 保存菜品口味信息到表 dish_flavor
        var dishId = dishDto.getId();
        var flavors = dishDto.getFlavors();
        for (var flavor : flavors)
            flavor.setDishId(dishId);
        dishFlavorService.saveBatch(flavors);
    }

    public DishDto getByIdWithFlavor(Long id) {
        // 查询菜品基本信息, 从 dish 表查询
        var dish = this.getById(id);
        var disDto = new DishDto();
        BeanUtils.copyProperties(dish, disDto);

        var lqw = new LambdaQueryWrapper<DishFlavor>();
        lqw.eq(DishFlavor::getDishId, dish.getId());
        var flavors = dishFlavorService.list(lqw);
        disDto.setFlavors(flavors);

        return disDto;
    }

    public void updateWithFlavor(DishDto dishDto) {
        // 更新菜品基本信息到表 dish
        this.updateById(dishDto);

        // 清理当前菜品对应的口味数据
        var dishId = dishDto.getId();
        var lqw = new LambdaQueryWrapper<DishFlavor>();
        lqw.eq(DishFlavor::getDishId, dishId);
        dishFlavorService.remove(lqw);

        var flavors = dishDto.getFlavors();
        flavors.forEach(flavor -> flavor.setDishId(dishId)); // 流式 API
        dishFlavorService.saveBatch(flavors);
    }
}
