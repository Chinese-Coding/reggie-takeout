package cn.bupt.edu.zfq.reggietakeout.entity.dto;

import cn.bupt.edu.zfq.reggietakeout.entity.User;
import lombok.Data;

@Data
public class UserDto extends User {
    private String code;
}
