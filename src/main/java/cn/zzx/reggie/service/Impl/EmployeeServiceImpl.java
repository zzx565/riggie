package cn.zzx.reggie.service.Impl;

import cn.zzx.reggie.mapper.EmployeeMapper;
import cn.zzx.reggie.entity.Employee;
import cn.zzx.reggie.service.EmployeeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
