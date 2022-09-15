package cn.zzx.reggie.service;

import cn.zzx.reggie.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
