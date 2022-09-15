package cn.zzx.reggie.controller;

import cn.zzx.reggie.common.R;
import cn.zzx.reggie.dto.DishDto;
import cn.zzx.reggie.entity.Category;
import cn.zzx.reggie.entity.Dish;
import cn.zzx.reggie.entity.DishFlavor;
import cn.zzx.reggie.service.CategoryService;
import cn.zzx.reggie.service.DishFlavorService;
import cn.zzx.reggie.service.DishService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// 菜品管理
@RestController
@RequestMapping("/dish")
public class DishController {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DishController.class);
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    // 新增菜品
    // 因为页面传输的数据比较复杂，传输数据和实体类无法一一对应，所以需要DTO来进行传输
    //DTO，全称Data Transfer Object，即数据传输对象，一般用于展示层与服务层之间的数据传输。

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    // 菜品信息分页查询
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        // 构造分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        // 条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 查询条件
        queryWrapper.like(name !=null,Dish::getName,name);
        // 排序
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo,queryWrapper);

        // 由于给页面的数据有菜品分类名称，而dish只有分类id，所以使用DishDTO
        Page<DishDto> dishDtoPage = new Page<>();
        // 对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        // 拿到数据列表
        List<Dish> recodes = pageInfo.getRecords();

        // 将数据列表给到dishDtoPage，同时加上菜品分类名称
        List<DishDto> list= recodes.stream().map((item) ->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId(); // 分类id
            // 根据id查到分类名称
            Category category = categoryService.getById(categoryId);
            if(category !=null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    // 根据id查询菜品信息和对应口味信息
    @GetMapping("/{id}")
     public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    // 修改菜品
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功");
    }

    // 修改菜品状态，其实就是更新
    @PostMapping("/status/{status}")
    public R<String> statusWithIds(@PathVariable("status") Integer status,@RequestParam List<Long> ids){
        // 构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(status !=null,Dish::getId,ids);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        // 单个菜品
        //Dish dish = dishService.getById(queryWrapper);

        //根据条件进行批量修改
        List<Dish> list = dishService.list(queryWrapper);
        for (Dish dish : list) {
            if (dish != null) {
                //把浏览器传入的status参数赋值给菜品
                dish.setStatus(status);
                dishService.updateById(dish);
            }
        }

        return R.success("售卖状态修改成功");
    }

    // 删除操作以及批量删除,停售才可以删除
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        dishService.deleteWithIds(ids);
        return R.success("删除成功");
    }

    /*// 根据条件查询对应菜品信息
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        // 构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() !=null,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1(起售)的菜品
        queryWrapper.eq(Dish::getStatus,1);

        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        return R.success(list);
    }*/

    // 根据条件查询对应菜品信息,由于前端需要显示口味，所以需要对原先的进行修改，传回的值为DishDto
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        // 构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() !=null,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1(起售)的菜品
        queryWrapper.eq(Dish::getStatus,1);

        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        // 新增
        List<DishDto> dishDtoList = list.stream().map((item) ->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId(); // 分类id
            // 根据id查到分类名称
            Category category = categoryService.getById(categoryId);
            if(category !=null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            // 获取当前菜品id
            Long dishId = item.getId();
            // 根据菜品获取口味
            LambdaQueryWrapper<DishFlavor> flavorQueryWrapper=new LambdaQueryWrapper<>();
            flavorQueryWrapper.eq(DishFlavor::getDishId,dishId);
            // SQL: select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(flavorQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }
}
