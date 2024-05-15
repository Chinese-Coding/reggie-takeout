package cn.bupt.edu.zfq.reggietakeout.entity.dto;


import cn.bupt.edu.zfq.reggietakeout.entity.Order;
import cn.bupt.edu.zfq.reggietakeout.entity.OrderDetail;
import lombok.Data;

import java.util.List;

@Data
public class OrderDto extends Order {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;

}
