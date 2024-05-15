package cn.bupt.edu.zfq.reggietakeout.service;


import cn.bupt.edu.zfq.reggietakeout.controller.ShoppingCartController;
import cn.bupt.edu.zfq.reggietakeout.entity.*;
import cn.bupt.edu.zfq.reggietakeout.mapper.OrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderService extends ServiceImpl<OrderMapper, Order> {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private ShoppingCartController shoppingCartController;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 订单提交 (下单)
     */
    public void submit(Order order, HttpSession session) {
        // 获取用户Id
        var userId = (Long) session.getAttribute("user");
        // 根据用户Id查询所对应的购物车内容
        var lqw = new LambdaQueryWrapper<ShoppingCart>();
        lqw.eq(userId != null, ShoppingCart::getUserId, userId);
        var shoppingCartList = shoppingCartService.list(lqw);

        // 如果订单是空就没必要下单了
        if (shoppingCartList == null || shoppingCartList.isEmpty())
            throw new RuntimeException("购物车为空，不能下单");

        //拿到用户的地址数据
        var addressId = order.getAddressBookId();
        var addressBook = addressBookService.getById(addressId);
        if (addressBook == null)
            throw new RuntimeException("地址有误，不能下单");

        var orderId = IdWorker.getId();  // 订单号

        // 遍历购物车列表，来算一下总金额
        // 购物车中商品的总金额需要保证在多线程的情况下也是能计算正确的, 故需要使用原子类
        var amount = new AtomicInteger(0);

        var orderDetails = shoppingCartList.stream().map((item) -> {
            var orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetMealId(item.getSetMealId());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        //填充订单对象信息
        order.setId(orderId);
        order.setNumber(String.valueOf(orderId));
        order.setStatus(2);
        order.setUserId(userId);
        // address_book_id
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(LocalDateTime.now());
        // pay_method
        order.setAmount(new BigDecimal(amount.get()));//总金额, 需要遍历购物车, 计算相关金额来得到
        // remark
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail() == null ? "" : addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());

        // 向订单表插入数据,一条数据，插入数据之前，需要填充如上属性
        this.save(order);

        // 向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        // 调用接口清空购物车
        shoppingCartController.clean(session);
    }
}
