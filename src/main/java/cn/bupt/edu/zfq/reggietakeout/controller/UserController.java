package cn.bupt.edu.zfq.reggietakeout.controller;


import cn.bupt.edu.zfq.reggietakeout.common.R;
import cn.bupt.edu.zfq.reggietakeout.entity.User;
import cn.bupt.edu.zfq.reggietakeout.entity.dto.UserDto;
import cn.bupt.edu.zfq.reggietakeout.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;


@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;


    /**
     * 验证码发送
     *
     * @param user    接收用户电话号码
     * @param session 把验证码存入session，后续登陆验证要用
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        var userPhone = user.getPhone();
        // 判断手机号是否为空
        if (StringUtils.isNotEmpty(userPhone)) {
            var code = generateValidateCode4String(4);
            // 这里不太可能去真的发验证码，所以把生成的验证码在后台看一眼就好
            log.info("手机号Phone: {}   验证码Code: {}", userPhone, code);
            //如果要发短信应该出现的代码
            //SMSUtils.sendMessage("外卖", "模板", userPhone, code);
            //把验证码存入Session，验证用，phone 为Key，code 为value
            session.setAttribute(userPhone, code);
            //将验证码存入redis，并设置好失效时间为5分钟
            //redisTemplate.opsForValue().set(userPhone, code, 5, TimeUnit.MINUTES);
            return R.success("验证码发送成功，有效时间为5分钟");
        }
        return R.error("验证码发送失败");
    }


    /**
     * 前台登陆功能
     *
     * @param userDto 对User类进行了扩展，原有user类没有code属性
     * @param session 从session中拿code（验证码），方便后需验证
     */
    @PostMapping("/login")
    public R<String> login(@RequestBody UserDto userDto, HttpSession session) {
        // 拿到验证码和手机号
        String code = userDto.getCode(), phone = userDto.getPhone();
        // 从session中拿到对应的验证码
        var tempCode = (String) session.getAttribute(phone);

        // 从Redis中拿验证
        // String tempCode = (String) redisTemplate.opsForValue().get(phone);

        //验证码相等
        if (code.equals(tempCode)) {
            // 是否为新用户，如果是新用户顺手注册了
            var lqw = new LambdaQueryWrapper<User>();
            lqw.eq(User::getPhone, phone);
            // 只能用getOne来匹配，不能用getById，因为没有Id给你匹配，都是空的
            var user = userService.getOne(lqw);
            if (user == null) {
                // 用户不存在，注册一下，注册完放行
                // 用户的ID是有自动生成策略的，不用管
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user", user.getId());
            return R.success("登陆成功，欢迎~");
        }
        return R.error("验证码错误");
    }
        /**
         * 随机生成指定长度字符串验证码
         *
         * @param length 长度
         */
        public static String generateValidateCode4String ( int length){
            var rdm = new Random();
            var hash1 = Integer.toHexString(rdm.nextInt());
            return hash1.substring(0, length);
        }
    }

