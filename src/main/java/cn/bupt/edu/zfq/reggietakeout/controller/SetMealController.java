package cn.bupt.edu.zfq.reggietakeout.controller;


import cn.bupt.edu.zfq.reggietakeout.common.R;
import cn.bupt.edu.zfq.reggietakeout.entity.SetMeal;
import cn.bupt.edu.zfq.reggietakeout.entity.dto.SetMealDto;
import cn.bupt.edu.zfq.reggietakeout.service.CategoryService;
import cn.bupt.edu.zfq.reggietakeout.service.SetMealService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/setMeal")
public class SetMealController {
    @Autowired
    private SetMealService setMealService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     */
    @PostMapping
    public R<String> save(@RequestBody SetMealDto setMealDto) {
        log.info("套餐信息: {}", setMealDto);
        setMealService.saveWithDish(setMealDto);
        return R.success("新增菜品成功");
    }

    @PutMapping
    public R<String> update(@RequestBody SetMealDto setMealDto) {
        setMealService.updateWithDish(setMealDto);
        return R.success("修改套餐成功");
    }

    /**
     * 更新菜品停售起售状态
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, String ids) {
        var idsList = ids.split(",");
        for (var id : idsList) {
            var setMeal = new SetMeal();
            setMeal.setId(Long.valueOf(id));
            setMeal.setStatus(status);
            setMealService.updateById(setMeal);
        }
        return R.success("更新成功");
    }

    /**
     * 套餐分页查询
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        var setMealPage = new Page<SetMeal>(page, pageSize);
        var setmealDtoPage = new Page<SetMealDto>(page, pageSize);
        var lqw = new LambdaQueryWrapper<SetMeal>();
        lqw.like(name != null, SetMeal::getName, name);
        lqw.orderByDesc(SetMeal::getUpdateTime);
        setMealService.page(setMealPage, lqw);

        BeanUtils.copyProperties(setMealPage, setmealDtoPage, "records");

        var records = setMealPage.getRecords();
        var list = records.stream().map(
                (item) -> {
                    var setmealDto = new SetMealDto();
                    BeanUtils.copyProperties(item, setmealDto);

                    // 获取分类的名字并赋值给 setMealDto
                    var categoryId = item.getCategoryId();
                    var category = categoryService.getById(categoryId);
                    if (category != null)
                        setmealDto.setCategoryName(category.getName());
                    return setmealDto;
                }
        ).toList();
        setmealDtoPage.setRecords(list);
        return R.success(setmealDtoPage);
    }

    /**
     * 删除套餐
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        setMealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * 拿到套餐信息，回填前端页面，为后续套餐更新做准备，调用Service层写
     *
     * @param id ResultFul风格传入参数，接收套餐id对象，用@PathVariable来接收同名参数
     * @return 返回套餐对象
     */
    @GetMapping("/{id}")
    public R<SetMealDto> getSetMeal(@PathVariable("id") Long id) {
        log.info("获取套餐Id {}", id);
        var setMealDto = setMealService.getSetMealWithDish(id);
        return R.success(setMealDto);
    }
}
