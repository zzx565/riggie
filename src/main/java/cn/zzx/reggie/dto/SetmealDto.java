package cn.zzx.reggie.dto;

import cn.zzx.reggie.entity.Setmeal;
import cn.zzx.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
