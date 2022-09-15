package cn.zzx.reggie.service.Impl;

import cn.zzx.reggie.entity.User;
import cn.zzx.reggie.mapper.UserMapper;
import cn.zzx.reggie.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
