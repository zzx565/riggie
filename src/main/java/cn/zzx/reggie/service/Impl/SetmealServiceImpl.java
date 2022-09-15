package cn.zzx.reggie.service.Impl;

import cn.zzx.reggie.common.CustomException;
import cn.zzx.reggie.dto.SetmealDto;
import cn.zzx.reggie.mapper.SetmealMapper;
import cn.zzx.reggie.entity.Setmeal;
import cn.zzx.reggie.entity.SetmealDish;
import cn.zzx.reggie.service.SetmealDishService;
import cn.zzx.reggie.service.SetmealService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    SetmealDishService setmealDishService;

    // 新增套餐，同时需要保存套餐和菜品的关联关系
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        // 1.保存套餐的基本信息，操作setmeal表
        this.save(setmealDto);

        // 3.因为setmealId没有值，所以要赋值
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        // 2.保存套餐和菜品的关联信息，操作setmeal_dish表
        setmealDishService.saveBatch(setmealDishes);


    }

    // 删除套餐，同时删除套餐和菜品的关联状态
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // select count(*) from setmeal where id in (1,2,3) and status = 1
        // 查询状态
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);
        if (count > 0){
            //不能删除， 抛出业务异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        // 如果可以删除，删除套餐中的数据-- setmeal
        this.removeByIds(ids);

        // delete from setmeal_dish where setmealId in (1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        // 删除关系表中的数据-- setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);

    }

    // 根据id来查询套餐信息和对应的菜品信息
    public SetmealDto getByIdWithDith(Long id) {
        //1.查询套餐的基本信息，从setmeal表中查询
        Setmeal setmeal = this.getById(id);

        //2.查询当前套餐对应的菜品信息，从setmeal_dish表中查询
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //根据套餐的id查对应的菜品
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());

        List<SetmealDish> dishs = setmealDishService.list(queryWrapper);
        SetmealDto setmealDto = new SetmealDto();
        // 对象拷贝
        BeanUtils.copyProperties(setmeal, setmealDto);
        setmealDto.setSetmealDishes(dishs);
        return setmealDto;
    }

    // 更新套餐信息，并更新关联菜品
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        // 1.更新套餐基本信息
        this.updateById(setmealDto);

        // 2.删除原先菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper =new LambdaQueryWrapper<>();
        // 根据id删除对应菜品信息
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        // 3.添加新的菜品信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);

    }
}

