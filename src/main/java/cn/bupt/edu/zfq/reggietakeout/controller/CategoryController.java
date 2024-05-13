package cn.bupt.edu.zfq.reggietakeout.controller;

import cn.bupt.edu.zfq.reggietakeout.common.R;
import cn.bupt.edu.zfq.reggietakeout.entity.Category;
import cn.bupt.edu.zfq.reggietakeout.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 分类信息分页查询
     */

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        var pageInfo = new Page(page, pageSize); // 分页构造器
        var queryWrapper = new LambdaQueryWrapper<Category>(); // 条件构造器
        queryWrapper.orderByAsc(Category::getSort); // 添加排序条件
        categoryService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据条件查询分类数据
     * @param category 分类信息, 目前只有 type 字段有效, 不用加注解 `@RequestBody`, 因为前端是通过 url 后面携带参数传参的
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        var lqw = new LambdaQueryWrapper<Category>();
        lqw.eq(category.getType() !=null, Category::getType, category.getType());
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        var list = categoryService.list(lqw);
        return R.success(list);
    }

    /**
     * 新增分类
     */
    @PostMapping
    public R<String> add(@RequestBody Category category) {
        log.info("category: {}", category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    @DeleteMapping
    public R<String> delete(Long id) {
        log.info("删除 id 为 {} 的菜品", id);
        categoryService.remove(id);
        return R.success("分类信息删除成功");
    }

    /**
     * 根据 id 修改分类信息
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("修改分类信息: {}",category);
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }
}