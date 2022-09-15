package cn.zzx.reggie.service.Impl;

import cn.zzx.reggie.entity.AddressBook;
import cn.zzx.reggie.mapper.AddressBookMapper;
import cn.zzx.reggie.service.AddressBookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
