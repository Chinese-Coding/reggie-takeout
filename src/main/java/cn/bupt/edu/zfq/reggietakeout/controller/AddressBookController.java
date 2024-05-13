package cn.bupt.edu.zfq.reggietakeout.controller;


import cn.bupt.edu.zfq.reggietakeout.common.R;
import cn.bupt.edu.zfq.reggietakeout.entity.AddressBook;
import cn.bupt.edu.zfq.reggietakeout.service.AddressBookService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 地址簿管理
 * <p>
 * TODO: 将前后端地址簿关于 sex 之间的转换合并到实体类中, 形成一个方法
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook, HttpSession session) {
        var userId = (Long) session.getAttribute("user");
        addressBook.setUserId(userId);
        if (addressBook.getSex().equals("1"))
            addressBook.setSex("male");
        else if (addressBook.getSex().equals("0"))
            addressBook.setSex("female");
        log.info("addressBook:{}", addressBook);
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }


    /**
     * 设置默认地址
     */
    @PutMapping("default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook, HttpSession session) {
        var userId = (Long) session.getAttribute("user");
        log.info("addressBook:{}", addressBook);
        var lqw = new LambdaUpdateWrapper<AddressBook>();
        lqw.eq(AddressBook::getUserId, userId);
        lqw.set(AddressBook::getDefaultAddress, false);
        addressBookService.update(lqw);
        addressBook.setDefaultAddress(true);
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }


    /**
     * 查询默认地址
     */
    @GetMapping("default")
    public R<AddressBook> getDefault(HttpSession session) {
        var userId = (Long) session.getAttribute("user");
        var queryWrapper = new LambdaQueryWrapper<AddressBook>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        queryWrapper.eq(AddressBook::getDefaultAddress, true);
        var addressBook = addressBookService.getOne(queryWrapper);

        if (null == addressBook)
            return R.error("没有找到该对象");
        else
            return R.success(addressBook);
    }


    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        var addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            // 处理前后端数据交互的问题
            if (addressBook.getSex().equals("male"))
                addressBook.setSex("1");
            else if (addressBook.getSex().equals("female"))
                addressBook.setSex("0");
            return R.success(addressBook);
        } else
            return R.error("没有找到该对象");
    }

    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook) {
        if (addressBook.getSex().equals("1"))
            addressBook.setSex("male");
        else if (addressBook.getSex().equals("0"))
            addressBook.setSex("female");
        addressBookService.updateById(addressBook);
        return R.success("修改成功");
    }

    /**
     * 查询指定用户的全部地址
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook, HttpSession session) {
        var userId = (Long) session.getAttribute("user");
        addressBook.setUserId(userId);
        log.info("addressBook:{}", addressBook);
        var lqw = new LambdaQueryWrapper<AddressBook>();
        lqw.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
        lqw.orderByDesc(AddressBook::getUpdateTime);
        var addressBooks = addressBookService.list(lqw);
        for (var addressBook1 : addressBooks) {
            if (addressBook1.getSex().equals("male"))
                addressBook1.setSex("1");
            else if (addressBook1.getSex().equals("female"))
                addressBook1.setSex("0");
        }
        return R.success(addressBooks);
    }
}
