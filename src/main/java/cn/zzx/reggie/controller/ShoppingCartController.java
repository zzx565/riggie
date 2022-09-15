package cn.zzx.reggie.controller;

import cn.zzx.reggie.common.BaseContext;
import cn.zzx.reggie.common.R;
import cn.zzx.reggie.entity.ShoppingCart;
import cn.zzx.reggie.service.ShoppingCartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

// 前端购物车

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    // 向购物车添加
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车信息：{}",shoppingCart);
        // 设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        // 判断添加到购物车的是菜品还是套餐
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        Long dishId = shoppingCart.getDishId();
        if(dishId !=null){
            // 是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            // 是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        // 查询菜品或套餐是否在数据库
        // SQL：select * from shopping_cart where user_Id = ? and dish_id/setmeal_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        if(cartServiceOne !=null){
            // 如果在，则数量加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number +1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            // 如果不在，则加入数据库
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }

        return R.success(cartServiceOne);
    }

    // 查看购物车
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车");
        LambdaQueryWrapper<ShoppingCart> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    // 清空购物车
    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }

    // 减少购物车数量
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        log.info("需要减少的商品：{}",shoppingCart);
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper =new LambdaQueryWrapper<>();
        // 获取操作的是哪个用户的购物车
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        // 判断减少的是菜品还是套餐
        if(dishId !=null){
            // 减少的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            // 减少的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        cartServiceOne.setNumber(cartServiceOne.getNumber()- 1);
        Integer number = cartServiceOne.getNumber();
        // 判断数量
        if(number >0){
            // 数量不为0，更新购物车数量
            shoppingCartService.updateById(cartServiceOne);
        }else{
            // 数量为0，移除
            shoppingCartService.remove(queryWrapper);
        }
        return R.success(cartServiceOne);
    }
}
