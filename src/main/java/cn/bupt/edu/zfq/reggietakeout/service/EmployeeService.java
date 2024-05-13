package cn.bupt.edu.zfq.reggietakeout.service;

import cn.bupt.edu.zfq.reggietakeout.entity.Employee;
import cn.bupt.edu.zfq.reggietakeout.mapper.EmployeeMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService extends ServiceImpl<EmployeeMapper, Employee>  {
}