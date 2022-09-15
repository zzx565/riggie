package cn.zzx.reggie.controller;

import cn.zzx.reggie.common.R;
import cn.zzx.reggie.dto.SetmealDto;
import cn.zzx.reggie.entity.Category;
import cn.zzx.reggie.entity.Dish;
import cn.zzx.reggie.entity.Setmeal;
import cn.zzx.reggie.entity.SetmealDish;
import cn.zzx.reggie.service.CategoryService;
import cn.zzx.reggie.service.DishService;
import cn.zzx.reggie.service.SetmealDishService;
import cn.zzx.reggie.service.SetmealService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.beans.BeanUtils.copyProperties;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;

    // 新增套餐
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    // 套餐分页查询
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件
        queryWrapper.like(name != null, Setmeal::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);

        // 对象拷贝
        copyProperties(pageInfo, dtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            // 对象拷贝
            BeanUtils.copyProperties(item,setmealDto);
            // 获取分类id
            Long categoryId = item.getCategoryId();
            // 根据分类id 获取分类名称
            Category category = categoryService.getById(categoryId);
            if (category !=null){
                // 分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    // 删除套餐
    @DeleteMapping
    public R<String> delete(@RequestParam  List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    // 套餐修改页面展示
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDith(id);
        return  R.success(setmealDto);
    }

    // 更新套餐，同时更新关联菜品信息
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        setmealService.updateWithDish(setmealDto);
        return R.success("修改套餐成功");
    }

    // 批量停售起售
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable int status,@RequestParam List<Long> ids){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.orderByDesc(Setmeal::getPrice);
        // 根据条件进行批量查询
        List<Setmeal> list = setmealService.list(queryWrapper);
        list.stream().map((item)->{
            item.setStatus(status);
            setmealService.updateById(item);
            return item;
        });
        return R.success("售卖状态修改成功");
    }

    // 移动端根据条件显示套餐信息
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,1);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

    // 移动端查看套餐详情
    @GetMapping("/dish/{id}")
    //使用路径来传值的
    public R<List<Dish>> dish(@PathVariable("id") Long SetmealId) {

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, SetmealId);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        LambdaQueryWrapper<Dish> queryWrapper2 = new LambdaQueryWrapper<>();
        ArrayList<Long> dishIdList = new ArrayList<>();
        for (SetmealDish setmealDish : list) {
            Long dishId = setmealDish.getDishId();
            dishIdList.add(dishId);
        }
        queryWrapper2.in(Dish::getId, dishIdList);
        List<Dish> dishList = dishService.list(queryWrapper2);

        return R.success(dishList);
    }
}
