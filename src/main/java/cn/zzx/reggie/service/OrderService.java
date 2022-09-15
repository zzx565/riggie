package cn.zzx.reggie.service;

import cn.zzx.reggie.dto.OrdersDto;
import cn.zzx.reggie.entity.Orders;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrderService extends IService<Orders> {

     Page<OrdersDto> empPage(int page, int pageSize, String number, String beginTime, String endTime);

    void submit(Orders orders);

    Page<OrdersDto> userPage(int page, int pageSize);

    void again(Orders order);
}
