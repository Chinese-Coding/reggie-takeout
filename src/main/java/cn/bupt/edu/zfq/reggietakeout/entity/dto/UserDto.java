package cn.bupt.edu.zfq.reggietakeout.entity.dto;

import cn.bupt.edu.zfq.reggietakeout.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDto extends User {
    private String code;
}
