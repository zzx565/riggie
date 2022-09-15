package cn.zzx.reggie.controller;

import cn.zzx.reggie.common.R;
import cn.zzx.reggie.entity.Category;
import cn.zzx.reggie.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 分类管理
@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    // 新增分类
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("category:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    //分页查询
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        // 构造分页构造器
        Page<Category> pageInfo = new Page<>(page,pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 根据sort进行排序
        queryWrapper.orderByAsc(Category::getSort);

        // 查询
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);

    }


    // 删除分类的时候如果分类有关联菜品或套餐则无法删除
    @DeleteMapping
    public R<String> delete(long ids){
        log.info("删除分类的id:{}",ids);

        // categoryService.removeById(id);
        categoryService.remove(ids);
        return R.success("分类删除成功");
    }

    // 根据id修改分类
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改的分类id: {}",category);
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    // 根据条件查询分类数据
    @GetMapping("/list")
    public R<List<Category>> queryList(Category category){  // 参数也可以是String type
        // 条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 添加条件
        queryWrapper.eq(category.getType() !=null,Category::getType,category.getType());
        // 添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
