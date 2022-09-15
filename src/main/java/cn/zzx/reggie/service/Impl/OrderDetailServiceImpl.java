package cn.zzx.reggie.service.Impl;

import cn.zzx.reggie.entity.OrderDetail;
import cn.zzx.reggie.mapper.OrderDetailMapper;
import cn.zzx.reggie.service.OrderDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
