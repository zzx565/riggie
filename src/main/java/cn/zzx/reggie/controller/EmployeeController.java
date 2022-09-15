package cn.zzx.reggie.controller;


import cn.zzx.reggie.common.R;
import cn.zzx.reggie.entity.Employee;
import cn.zzx.reggie.service.EmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // 员工登录
    @PostMapping("login")
    public R<Employee> login(HttpSession session, @RequestBody Employee employee){

        // 1、将页面提交的密码password进行md加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        // 因为username在数据库设置为唯一，所以可以使用getOne()来拿到
        Employee emp = employeeService.getOne(queryWrapper);


        // 3、如果没有查询到则返回登录失败结果
        if(emp == null){
            return R.error("登陆失败");
        }

        //4、密码对比，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("登陆失败");
        }

        // 5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus()==0){
            return R.error("账号已被禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        session.setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    // 员工退出
    @RequestMapping("/logout")
    public R<String> logout(HttpSession session){
        // 清除session中保存的id
        session.removeAttribute("employee");
        return R.success("退出成功");
    }

    // 新增员工
    /*
      数据库设计了员工账号唯一，所以在新增员工的时候如果账号重复会报一个sql异常
      处理方法： 1. 抛出异常
                2. 设置一个全局异常处理类GlobalExceptionHandler(本项目使用此方法)
     */
    @PostMapping
    public R<String> add(HttpSession session,@RequestBody Employee employee){
        log.info("新增员工：{}",employee.toString());
        // 设置默认密码123456，需要md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        // 设置增加时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//
        // 设置新增员工
//        Long empId = (Long) session.getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        // 保存
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    // 分页查询
    // 使用了MP的分页插件，需要先配置MybatisPlusConfig
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);

        // 构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件（当name不为空是添加条件）
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        // 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    // 修改员工信息
    /*
    这里需要注意：js处理long型数字只能精确到16位，而我们的id有19位，这会造成页面传递过来的id
    精确度丢失。
    处理方法：服务端给页面响应json数据时进行处理，将long类型的转换为String类型
           自定义一个json的处理器映射器JacksonObjectMapper，加到mvc处理器集合中
     */
    @PutMapping
    public R<String> update(HttpSession session,@RequestBody Employee employee){
        log.info("employee:{}",employee);
        employee.setUpdateTime(LocalDateTime.now());
        //Long empId = (Long) session.getAttribute("employee");
        //employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    // 根据id查询员工信息
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if(employee !=null)
            return R.success(employee);
        return R.error("没有查询到对应的用户信息");
    }

}
