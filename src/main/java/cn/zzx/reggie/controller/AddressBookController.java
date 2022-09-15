package cn.zzx.reggie.controller;

import cn.zzx.reggie.common.BaseContext;
import cn.zzx.reggie.common.R;
import cn.zzx.reggie.entity.AddressBook;
import cn.zzx.reggie.service.AddressBookService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 地址簿管理
@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    // 新增地址簿
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        //设置userId，知道当前这个地址是谁的
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    //设置默认地址:将用户所有地址设为非默认地址(0),再将指定地址设置为默认地址(1)
    @PutMapping("default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        log.info("addressBook:{}", addressBook);
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        //根据当前登录用户id去匹配地址薄地址
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        //设置当前默认地址状态为 0  （0不是默认地址）
        wrapper.set(AddressBook::getIsDefault, 0);
        //SQL:update address_book set is_default = 0 where user_id = ?
        addressBookService.update(wrapper);

        //把当前要改的这条数据的地址状态改成1（1默认地址）
        addressBook.setIsDefault(1);
        //SQL:update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    //根据id查询地址
    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        } else {
            return R.error("没有找到该对象");
        }
    }

    // 查询默认地址
    @GetMapping("default")
    public R<AddressBook> getDefault() {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        //根据当前登录的UserID来查
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        //条件查询，查地址状态为1的数据
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        //SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        if (null == addressBook) {
            return R.error("没有找到该对象");
        } else {
            return R.success(addressBook);
        }
    }

    // 查询指定用户的全部地址
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook) {
        //根据当前登录的UserID来查
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);

        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        //SQL:select * from address_book where user_id = ? order by update_time desc
        return R.success(addressBookService.list(queryWrapper));
    }

    // 修改地址
    @PutMapping
    public R<AddressBook> update(@RequestBody AddressBook addressBook) {
        log.info("addressBook:{}", addressBook);
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    // 删除地址
    @DeleteMapping
    public R<String> remove(Long ids) {
        log.info("ids:{}",ids);
        addressBookService.removeById(ids);
        return R.success("删除成功");
    }
}

