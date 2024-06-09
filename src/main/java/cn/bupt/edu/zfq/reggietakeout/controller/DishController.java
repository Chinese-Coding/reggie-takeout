package cn.bupt.edu.zfq.reggietakeout.controller;

import cn.bupt.edu.zfq.reggietakeout.common.R;
import cn.bupt.edu.zfq.reggietakeout.entity.Dish;
import cn.bupt.edu.zfq.reggietakeout.entity.DishFlavor;
import cn.bupt.edu.zfq.reggietakeout.entity.dto.DishDto;
import cn.bupt.edu.zfq.reggietakeout.service.CategoryService;
import cn.bupt.edu.zfq.reggietakeout.service.DishFlavorService;
import cn.bupt.edu.zfq.reggietakeout.service.DishService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        var dishPage = new Page<Dish>(page, pageSize);
        var dishDtoPage = new Page<DishDto>(page, pageSize);

        // 先把 dish 查出来
        var lqw = new LambdaQueryWrapper<Dish>();
        lqw.eq(Dish::getDeleted, false); // 必须是逻辑上没有被删除的
        lqw.like(name != null, Dish::getName, name); // 添加过滤条件
        lqw.orderByDesc(Dish::getUpdateTime); // 添加排序条件
        dishService.page(dishPage, lqw); // 执行分页查询

        BeanUtils.copyProperties(dishPage, dishDtoPage, "records"); // 除去 dish 中的 `records` 属性, 其余的先复制过去

        /*
         * 用循环查出, 对应菜品的分类, 并将其赋值给 一个 DishDto 对象, 并将其添加到一个 list 中
         * 这里使用了循环遍历查询, 而没有使用 sql 语句进行多表联查
         */
        var records = dishPage.getRecords();
        var list = new ArrayList<DishDto>();
        for (var record : records) {
            var dishDto = new DishDto();
            BeanUtils.copyProperties(record, dishDto);
            var category = categoryService.getById(record.getCategoryId());
            if (category != null) // 确保真的能查到一个 category, 以免发生空指针错误
                dishDto.setCategoryName(category.getName());
            list.add(dishDto);
        }
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /**
     * 新增菜品
     */
    @PostMapping
    public R<String> add(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 删除菜品信息, 但是只做逻辑上的删除
     */
    @DeleteMapping
    public R<String> remove(String ids) {
        var idsList = ids.split(",");
        for (var id : idsList) {
            var dish = new Dish();
            dish.setId(Long.valueOf(id));
            dish.setDeleted(true);
            dishService.updateById(dish);
        }
        return R.success("删除成功");
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 更新菜品起售或禁售信息
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, String ids) {
        var idsList = ids.split(",");
        for (var id : idsList) {
            var dish = new Dish();
            dish.setId(Long.valueOf(id));
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        return R.success("更新成功");
    }

    /**
     * 根据 id 查询菜品信息和对应的口味信息
     */
    @GetMapping("/{id}")
    public R<DishDto> getDishById(@PathVariable Long id) {
        return R.success(dishService.getByIdWithFlavor(id));
    }

    /**
     * 获取同一分类下的菜品信息, 使用 `Dish` 接收参数, 而不简简单单的使用 `Long` 接收一个 `categoryId`. 这样做, 据视频所说更通用一些
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        // 结果返回对象
        List<DishDto> dishDtoList;

        var lqw = new LambdaQueryWrapper<Dish>();
        lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        lqw.eq(Dish::getStatus, 1);
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        var dishList = dishService.list(lqw);
        //分类id
        //根据当前菜品的id查询菜品表下dishId对应的菜品
        var list = new ArrayList<DishDto>();
        for (var item : dishList) {
            var dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            var categoryId = item.getCategoryId();//分类id
            var category = categoryService.getById(categoryId);
            if (category != null)
                dishDto.setCategoryName(category.getName());

            // 根据当前菜品的id查询菜品表下dishId对应的菜品
            var dishId = item.getId();
            var dishFlavorLqw = new LambdaQueryWrapper<DishFlavor>();
            dishFlavorLqw.eq(DishFlavor::getDishId, dishId);
            dishDto.setFlavors(dishFlavorService.list(dishFlavorLqw));
            list.add(dishDto);
        }
        dishDtoList = list;

        return R.success(dishDtoList);
    }
}
