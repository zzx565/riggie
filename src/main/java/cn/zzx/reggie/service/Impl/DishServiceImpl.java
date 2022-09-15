package cn.zzx.reggie.service.Impl;

import cn.zzx.reggie.common.CustomException;
import cn.zzx.reggie.dto.DishDto;
import cn.zzx.reggie.mapper.DishMapper;
import cn.zzx.reggie.entity.Dish;
import cn.zzx.reggie.entity.DishFlavor;
import cn.zzx.reggie.service.DishFlavorService;
import cn.zzx.reggie.service.DishService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    // 新增菜品，同时保存口味数据
    // 操作多张表，添加事务支持
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishID = dishDto.getId();//菜品id

        List<DishFlavor> flavors = dishDto.getFlavors();    //菜品口味
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishID);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    // 根据id查询菜品基本信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id){
        // 查询菜品信息，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        // 查询口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;

    }

    // 更新菜品信息以及口味信息
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        // 更新菜品基本信息
        this.updateById(dishDto);

        // 清除旧的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        // 插入新的口味信息
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    // 删除菜品
    @Override
    @Transactional
    public void deleteWithIds(List<Long> ids) {
        //构造一个条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //判断浏览器传来的ids是否为空，并和菜品表中的id进行匹配
        queryWrapper.in(ids != null, Dish::getId,ids);
        List<Dish> list = this.list(queryWrapper);

        for (Dish dish : list) {
            //判断当前菜品是否在售卖阶段，0停售，1起售
            if (dish.getStatus() == 0) {
                //停售状态直接删除
                this.removeById(dish.getId());
                LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
                //根据菜品id匹配口味表中的菜品id
                dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dish.getId());
                //删除菜品id关联的口味表信息
                dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
            }else {
                // 起售状态，抛出一个业务异常
                throw new CustomException("此菜品还在售卖阶段，删除影响销售！");
            }
        }
    }


}

