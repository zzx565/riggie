package cn.zzx.reggie.controller;

import cn.zzx.reggie.common.R;
import cn.zzx.reggie.dto.OrdersDto;
import cn.zzx.reggie.entity.Orders;
import cn.zzx.reggie.service.OrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 订单
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    // 后台订单展示
    @GetMapping("/page")
    public R<Page<OrdersDto>> empPage(int page, int pageSize, String number,
                                      String beginTime,
                                      String endTime) {
        Page<OrdersDto> empPage = orderService.empPage(page, pageSize, number, beginTime, endTime);
        return R.success(empPage);
    }

    // 后台订单数据更新
    @PutMapping
    public R<String> update(@RequestBody Orders orders) {
        orderService.updateById(orders);
        return R.success("操作成功");
    }

    // 移动端提交订单
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    // 移动端查看订单
    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize) {
        Page<OrdersDto> dtoPage = orderService.userPage(page, pageSize);
        return R.success(dtoPage);
    }

    // 再来一单
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders order) {
        orderService.again(order);
        return R.success("再来一单");
    }

}
