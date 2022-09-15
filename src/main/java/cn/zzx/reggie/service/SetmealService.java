package cn.zzx.reggie.service;

import cn.zzx.reggie.dto.SetmealDto;
import cn.zzx.reggie.entity.Setmeal;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    // 新增套餐
    void saveWithDish(SetmealDto setmealDto);

    // 删除套餐，同时删除套餐和菜品的关联数据
    void removeWithDish(List<Long> ids);

    // 根据id来查询套餐信息和对应的菜品信息
    SetmealDto getByIdWithDith(Long id);

    void updateWithDish(SetmealDto setmealDto);
}
