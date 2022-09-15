package cn.zzx.reggie.service.Impl;

import cn.zzx.reggie.entity.ShoppingCart;
import cn.zzx.reggie.mapper.ShoppingCartMapper;
import cn.zzx.reggie.service.ShoppingCartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
