package cn.zzx.reggie.service.Impl;

import cn.zzx.reggie.common.CustomException;
import cn.zzx.reggie.mapper.CategoryMapper;
import cn.zzx.reggie.entity.Category;
import cn.zzx.reggie.entity.Dish;
import cn.zzx.reggie.entity.Setmeal;
import cn.zzx.reggie.service.CategoryService;
import cn.zzx.reggie.service.DishService;
import cn.zzx.reggie.service.SetmealService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @Override
    public void remove(Long id) {
        //查看分类是否关联菜品
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        // 查询当前分类是否关联了菜品，如果关联，抛出一个业务异常
        if(count1 >0){
            // 已经关联菜品，抛出异常
            throw  new CustomException("当前分类下关联了菜品，不能删除");
        }

        // 查看分类是否关联套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，根据分类id进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        // 查询当前分类是否关联了套餐，如果关联，抛出一个业务异常
        if(count2 >0){
            // 已经关联套餐，抛出异常
            throw  new CustomException("当前分类下关联了套餐，不能删除");
        }
        // 正常删除分类
        super.removeById(id);
    }

}
