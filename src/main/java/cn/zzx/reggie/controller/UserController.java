package cn.zzx.reggie.controller;

import cn.zzx.reggie.common.R;
import cn.zzx.reggie.entity.User;
import cn.zzx.reggie.service.UserService;
import cn.zzx.reggie.utils.SMSUtils;
import cn.zzx.reggie.utils.ValidateCodeUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

// 移动端用户
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        // 获取手机号
        String phone = user.getPhone();

        if(StringUtils.isNotEmpty(phone)){
            // 生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);

            // 调用阿里云提供的短信服务api发送短信
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            // 将生成的验证码保存，方便比较
            session.setAttribute("code",code);

            return R.success("验证码已发送");
        }
        return R.error("验证码发送失败");
    }


    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());
       // 获取手机号
        String phone = map.get("phone").toString();
        // 获取验证码
        String code = map.get("code").toString();

        // 从Session中获取保存的验证码
        Object codeInSession = session.getAttribute("code");

        // 进行验证码的对比
        if(codeInSession!=null &&codeInSession.equals(code)){
            // 验证码对比成功，查看手机是否在数据库中
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if(user ==null){  // 说明当前手机号没有注册过,保存到数据库中
                user = new User();
                user.setPhone(phone);

                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登陆失败，请重新登录");
    }

    // 用户退出
    @PostMapping("loginout")
    public R<String> logout(HttpSession session){
        session.removeAttribute("user");
        return R.success("退出成功");
    }
}
