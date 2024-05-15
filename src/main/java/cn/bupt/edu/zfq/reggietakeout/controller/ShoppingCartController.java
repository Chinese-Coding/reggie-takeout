package cn.bupt.edu.zfq.reggietakeout.controller;


import cn.bupt.edu.zfq.reggietakeout.common.R;
import cn.bupt.edu.zfq.reggietakeout.entity.ShoppingCart;
import cn.bupt.edu.zfq.reggietakeout.service.ShoppingCartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;


    /**
     * 往购物车内部添加
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart, HttpSession session) {
        log.info("购物车数据:{}", shoppingCart.toString());
        var userId = (Long) session.getAttribute("user");
        shoppingCart.setUserId(userId);
        // 判断当前传来的Id是菜品还是套, 这两个肯定会有一个是Null
        Long dishId = shoppingCart.getDishId(),
                setMealId = shoppingCart.getSetMealId();

        var lqw = new LambdaQueryWrapper<ShoppingCart>();
        lqw.eq(ShoppingCart::getUserId, userId);

        if (dishId != null)  // 传过来的是菜品而不是套餐
            lqw.eq(ShoppingCart::getDishId, dishId);
        if (setMealId != null) // 传过来的是套餐而不是菜品
            lqw.eq(ShoppingCart::getSetMealId, setMealId);


        // 如果可以查出来，说明购物车已经加入了相关菜品
        var cartServiceOne = shoppingCartService.getOne(lqw);

        if (cartServiceOne != null) { // 已经存在在购物车里, 在数量原有基础上 +1
            var count = cartServiceOne.getNumber();
            cartServiceOne.setNumber(count + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else { // 尚未存在购物车，就添加到购物车
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    @PostMapping("/sub")
    public R<String> sub(Long dishId, Long setMealId, HttpSession session) {
        var userId = (Long) session.getAttribute("user");
        var lqw = new LambdaQueryWrapper<ShoppingCart>();
        if (dishId != null) {
            lqw.eq(ShoppingCart::getDishId, dishId);
            lqw.eq(ShoppingCart::getUserId, userId);
            shoppingCartService.remove(lqw);
            return R.success("成功删除");
        } else if (setMealId != null) {
            lqw.eq(ShoppingCart::getSetMealId, setMealId);
            lqw.eq(ShoppingCart::getUserId, userId);
            shoppingCartService.remove(lqw);
            return R.success("成功删除");
        }
        return R.error("删除失败");
    }

    /**
     * @return 购物车列表
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(HttpSession session) {
        var userId = (Long) session.getAttribute("user");
        var lqw = new LambdaQueryWrapper<ShoppingCart>();
        lqw.eq(userId != null, ShoppingCart::getUserId, userId);

        // 最晚下单的 菜品或套餐在购物车中最先展示
        lqw.orderByDesc(ShoppingCart::getCreateTime);
        var list = shoppingCartService.list(lqw);

        return R.success(list);
    }

    /**
     * 一次性清空购物车
     */
    @DeleteMapping("/clean")
    public R<String> clean(HttpSession session) {
        var userId = (Long) session.getAttribute("user");
        //获取当前购物车用户Id
        var lqw = new LambdaQueryWrapper<ShoppingCart>();
        lqw.eq(ShoppingCart::getUserId, userId);
        shoppingCartService.remove(lqw);
        return R.success("清空成功");
    }
}
