package cn.zzx.reggie.service;

import cn.zzx.reggie.dto.DishDto;
import cn.zzx.reggie.entity.Dish;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface DishService extends IService<Dish> {
    // 新增菜品，同时保存口味
    void saveWithFlavor(DishDto dishDto);

    // 根据id查询菜品基本信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    // 更新菜品信息以及口味信息
    void updateWithFlavor(DishDto dishDto);

    // 删除菜品
    void deleteWithIds(List<Long> ids);
}
