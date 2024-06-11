package cn.bupt.edu.zfq.reggietakeout.controller;


import cn.bupt.edu.zfq.reggietakeout.common.R;
import cn.bupt.edu.zfq.reggietakeout.entity.Order;
import cn.bupt.edu.zfq.reggietakeout.entity.OrderDetail;
import cn.bupt.edu.zfq.reggietakeout.entity.dto.OrderDto;
import cn.bupt.edu.zfq.reggietakeout.service.OrderDetailService;
import cn.bupt.edu.zfq.reggietakeout.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 下订单
     * <p>
     * TODO: 测试
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Order order, HttpSession session) {
        // 比较繁琐, 在 service 实现
        orderService.submit(order, session);
        log.info("order: {}", order);
        return R.success("下单成功");
    }

    /**
     * 用户从已有订单中再来一单
     * <p>
     * TODO: 测试
     */
    @GetMapping("/again")
    public R<String> again(Long id, HttpSession session) {
        var order = orderService.getById(id);
        return this.submit(order, session);
    }

    /**
     * 用户分页查询订单
     * <p>
     * TODO: 测试
     */
    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize, HttpSession session) {
        var orderPageInfo = new Page<Order>(page, pageSize);
        var orderDtoPageInfo = new Page<OrderDto>(page, pageSize);
        var lqw = new LambdaQueryWrapper<Order>(); // 条件构造器
        lqw.eq(Order::getUserId, session.getAttribute("user"));
        lqw.orderByDesc(Order::getOrderTime); // 添加排序条件
        orderService.page(orderPageInfo, lqw);
        BeanUtils.copyProperties(orderPageInfo, orderDtoPageInfo, "records");
        var records = orderPageInfo.getRecords();
        var list = records.stream().map(
                (item) -> {
                    var orderDto = new OrderDto();
                    BeanUtils.copyProperties(item, orderDto);
                    var orderId = item.getId();
                    var orderDetailLqw = new LambdaQueryWrapper<OrderDetail>();
                    orderDetailLqw.eq(OrderDetail::getOrderId, orderId);
                    var orderDetail = orderDetailService.list(orderDetailLqw);
                    orderDto.setOrderDetails(orderDetail);
                    return orderDto;
                }
        ).toList();
        orderDtoPageInfo.setRecords(list);
        return R.success(orderDtoPageInfo);
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, LocalDateTime beginTime, LocalDateTime endTime) {
        var orderPageInfo = new Page<Order>(page, pageSize);
        var orderDtoPageInfo = new Page<OrderDto>(page, pageSize);
        var lqw = new LambdaQueryWrapper<Order>(); // 条件构造器
        lqw.eq(number != null, Order::getNumber, number)
                .between(beginTime != null && endTime != null, Order::getOrderTime, beginTime, endTime);

        lqw.orderByDesc(Order::getOrderTime); // 添加排序条件
        orderService.page(orderPageInfo, lqw);
        BeanUtils.copyProperties(orderPageInfo, orderDtoPageInfo, "records");
        var records = orderPageInfo.getRecords();
        var list = records.stream().map(
                (item) -> {
                    var orderDto = new OrderDto();
                    BeanUtils.copyProperties(item, orderDto);
                    var orderId = item.getId();
                    var orderDetailLqw = new LambdaQueryWrapper<OrderDetail>();
                    orderDetailLqw.eq(OrderDetail::getOrderId, orderId);
                    var orderDetail = orderDetailService.list(orderDetailLqw);
                    orderDto.setOrderDetails(orderDetail);
                    return orderDto;
                }
        ).toList();
        orderDtoPageInfo.setRecords(list);
        return R.success(orderDtoPageInfo);
    }

    @PutMapping
    public R<String> update(Order order) {
        orderService.updateById(order);
        return R.success("修改成功");
    }
}
