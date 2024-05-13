package cn.bupt.edu.zfq.reggietakeout.controller;

import cn.bupt.edu.zfq.reggietakeout.common.R;
import cn.bupt.edu.zfq.reggietakeout.entity.Employee;
import cn.bupt.edu.zfq.reggietakeout.service.EmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;


    /**
     * 员工登录
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        var password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        var lqw = new LambdaQueryWrapper<Employee>();
        lqw.eq(Employee::getUsername, employee.getUsername());
        var emp = employeeService.getOne(lqw);

        if (emp == null || !emp.getPassword().equals(password))
            return R.error("登录失败");
        if (emp.getStatus() == 0)
            return R.error("账号已禁用");

        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 员工信息分页查询
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        var pageInfo = new Page(page, pageSize); // 分页构造器
        var lqw = new LambdaQueryWrapper<Employee>(); // 条件构造器

        lqw.like(name != null, Employee::getName, name); // 添加过滤条件
        lqw.orderByDesc(Employee::getUpdateTime); // 添加排序条件

        employeeService.page(pageInfo, lqw);
        return R.success(pageInfo);
    }

    /**
     * 新增员工
     */
    @PostMapping
    public R<String> add(@RequestBody Employee employee) {
        log.info("新增员工: {}", employee);
        // 密码默认设置为 123456
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 根据员工 id 修改员工信息 (既可以用于修改所有信息, 又可以用于修改其中的部分信息)
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee) {
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
     * 根据 id 查询员工信息
     */
    @GetMapping("/{id}")
    public R<Employee> getEmpById(@PathVariable("id") Long id) {
        var employee = employeeService.getById(id);
        if (employee == null)
            return R.error("没有查询到对应员工信息");
        return R.success(employeeService.getById(id));
    }
}
