package cn.zzx.reggie.service.Impl;

import cn.zzx.reggie.mapper.DishFlavorMapper;
import cn.zzx.reggie.entity.DishFlavor;
import cn.zzx.reggie.service.DishFlavorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
