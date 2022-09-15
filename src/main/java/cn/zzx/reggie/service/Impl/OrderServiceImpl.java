package cn.zzx.reggie.service.Impl;

import cn.zzx.reggie.common.BaseContext;
import cn.zzx.reggie.common.CustomException;
import cn.zzx.reggie.dto.OrdersDto;
import cn.zzx.reggie.entity.*;
import cn.zzx.reggie.mapper.OrderMapper;
import cn.zzx.reggie.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private UserService userService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;

    // 后台订单展示
    @Override
    public Page<OrdersDto> empPage(int page, int pageSize, String number, String beginTime, String endTime) {

        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> pageDto = new Page<>();

        //创建条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件，根据number进行like模糊查询
        queryWrapper.like(number != null, Orders::getNumber, number);
        queryWrapper.gt(StringUtils.isNotEmpty(beginTime), Orders::getOrderTime, beginTime);
        queryWrapper.lt(StringUtils.isNotEmpty(endTime), Orders::getOrderTime, endTime);
        //添加排序条件(根据更新时间降序排序)
        queryWrapper.orderByDesc(Orders::getOrderTime);
        this.page(pageInfo, queryWrapper);
        //将其除了records中的内存复制到pageDto中
        BeanUtils.copyProperties(pageInfo, pageDto, "records");

        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> collect = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //对象拷贝
            BeanUtils.copyProperties(item, ordersDto);
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            //根据订单id查询订单详细信息
            lambdaQueryWrapper.eq(OrderDetail::getOrderId, item.getId());

            List<OrderDetail> orderDetails = orderDetailService.list(lambdaQueryWrapper);
            ordersDto.setOrderDetails(orderDetails);

            //根据userId查询用户姓名
            Long userID = item.getUserId();
            User user = userService.getById(userID);
            ordersDto.setUserName(user.getName());
            ordersDto.setPhone(user.getPhone());

            //获取地址信息
            Long addressBookId = item.getAddressBookId();
            AddressBook addressBook = addressBookService.getById(addressBookId);
            ordersDto.setAddress(addressBook.getDetail());
            ordersDto.setConsignee(addressBook.getConsignee());

            return ordersDto;
        }).collect(Collectors.toList());

        pageDto.setRecords(collect);
        return pageDto;
    }


    // 移动端提交订单
    @Override
    @Transactional  // 操作了订单表，订单明细表，购物车表
    public void submit(Orders orders) {
        // 获得当前用户id
        Long userId = BaseContext.getCurrentId();

        // 根据用户id获取用户购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException("购物车为空，不能下单");
        }

        // 查询用户信息:姓名
        User user = userService.getById(userId);

        // 查询地址数据：地址，收货人，手机号
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null) {
            throw new CustomException("用户地址信息有误，不能下单");
        }

        long orderId = IdWorker.getId(); // 生成订单号，mybatis-plus提供

        //原子操作，保证线程安全
        AtomicInteger amount = new AtomicInteger(0);

        // 1.计算总金额 2.订单明细数据
        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            //订单明细
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setOrderId(orderId); // 订单号
            orderDetail.setNumber(item.getNumber());  // 数量
            orderDetail.setDishFlavor(item.getDishFlavor());  // 菜品口味
            orderDetail.setDishId(item.getDishId());  // 菜品id
            orderDetail.setSetmealId(item.getSetmealId());  // 套餐id
            orderDetail.setName(item.getName());  // 菜品或套餐名称
            orderDetail.setImage(item.getImage()); // 图片
            orderDetail.setAmount(item.getAmount());    //单份的金额
            //累加
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        // 补充订单表数据: 地址id（已有）
        orders.setNumber(String.valueOf(orderId)); // 订单号
        orders.setId(orderId);  // id
        orders.setOrderTime(LocalDateTime.now()); // 下单时间
        orders.setCheckoutTime(LocalDateTime.now()); //结账时间
        orders.setStatus(2); // 订单状态 1待付款，2待派送，3已派送，4已完成，5已取消
        orders.setAmount(new BigDecimal(amount.get())); //总金额
        orders.setUserId(userId);  // 下单用户id
        orders.setUserName(user.getName()); // 用户名
        orders.setConsignee(addressBook.getConsignee()); // 收货人
        orders.setPhone(addressBook.getPhone()); // 手机号
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail())
        );


        // 向订单表插入数据，一条数据
        this.save(orders);

        // 向订单明细表插入数据，一条或多条数据
        orderDetailService.saveBatch(orderDetails);

        // 清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }

    // 移动端查看订单
    @Override
    public Page<OrdersDto> userPage(int page, int pageSize) {
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> dtoPage = new Page<>();
        //创建条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据更新时间降序排序
        queryWrapper.orderByDesc(Orders::getOrderTime);
        this.page(pageInfo, queryWrapper);

        //通过OrderId查寻对应的菜品/套餐
        LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
        //对OrderDto进行需要的属性赋值
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> list = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //为orderDetails属性赋值
            //获取订单ID
            Long orderId = item.getId();
            //根据订单ID查询对应的订单明细
            wrapper.eq(OrderDetail::getOrderId, orderId);
            List<OrderDetail> orderDetailList = orderDetailService.list(wrapper);
            BeanUtils.copyProperties(item, ordersDto);
            //对ordersDto的orderDetail属性进行赋值
            ordersDto.setOrderDetails(orderDetailList);

            return ordersDto;
        }).collect(Collectors.toList());

        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        dtoPage.setRecords(list);
        return dtoPage;
    }

    @Override
    public void again(Orders order) {
        //获取订单里订单号
        Orders orderId = this.getById(order.getId());
        String number = order.getNumber();

        //根据条件进行查询
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, number);
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);

        //根据查到的数据再次添加到购物车里
        List<ShoppingCart> list = orderDetailList.stream().map((item) -> {
            //把从order表中和order_details表中获取到的数据赋值给这个购物车对象
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setName(item.getName());
            shoppingCart.setImage(item.getImage());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setDishId(item.getDishId());
            shoppingCart.setSetmealId(item.getSetmealId());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartService.saveBatch(list);
    }

}
