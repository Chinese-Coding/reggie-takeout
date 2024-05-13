package cn.bupt.edu.zfq.reggietakeout.service;

import cn.bupt.edu.zfq.reggietakeout.common.AssociationDeletionException;
import cn.bupt.edu.zfq.reggietakeout.entity.Category;
import cn.bupt.edu.zfq.reggietakeout.entity.Dish;
import cn.bupt.edu.zfq.reggietakeout.entity.SetMeal;
import cn.bupt.edu.zfq.reggietakeout.mapper.CategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetMealService setMealService;


    public void remove(Long id) {
        // 查询当前分类是否关联了菜品, 如果已经关联, 抛出一个和业务异常
        var dishLqw = new LambdaQueryWrapper<Dish>();
        dishLqw.eq(Dish::getCategoryId, id);
        if (dishService.count(dishLqw) > 0)
            throw new AssociationDeletionException("当前分类关联了菜品, 不能删除");

        // 查询当前分类下是否关联了套餐, 如果已经关联, 抛出一个业务异常
        var setMealLqw = new LambdaQueryWrapper<SetMeal>();
        setMealLqw.eq(SetMeal::getCategoryId, id);
        if (setMealService.count(setMealLqw) > 0)
            throw new RuntimeException("当前分类下关联了套餐, 不能删除");

        super.removeById(id);
    }
}

