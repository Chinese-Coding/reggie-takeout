package cn.bupt.edu.zfq.reggietakeout.service;


import cn.bupt.edu.zfq.reggietakeout.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import cn.bupt.edu.zfq.reggietakeout.entity.User;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
}
