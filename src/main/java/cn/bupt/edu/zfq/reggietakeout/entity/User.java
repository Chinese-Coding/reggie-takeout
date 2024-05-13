package cn.bupt.edu.zfq.reggietakeout.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户信息
 */
@Data
@TableName("users") // 因为 user 是 PostgreSQL 的关键字, 所以这里改成 users
public class User {
    private Long id;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;

    private String avatar;

    private Integer status;
}
