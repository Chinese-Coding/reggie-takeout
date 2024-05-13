package cn.bupt.edu.zfq.reggietakeout.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 地址簿
 * <p>
 * 相对于源项目提供的 AddressBook 类, 缺少了部分字段, 因为他们在实际中并没有使用过
 */
@Data
public class AddressBook {
    private Long id;

    private Long userId; // 用户id

    private String consignee; // 收货人

    private String phone; // 手机号

    private String sex;

    private String detail; // 详细地址

    private String label; // 标签

    private Boolean defaultAddress; // 是否默认

    private Boolean deleted; // 是否删除

    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    //创建人
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    //修改人
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
