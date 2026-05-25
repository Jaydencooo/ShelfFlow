package com.shelfflow.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.shelfflow.constant.MessageConstant;
import com.shelfflow.constant.PasswordConstant;
import com.shelfflow.constant.StatusConstant;
import com.shelfflow.context.CurrentActorContext;
import com.shelfflow.dto.StaffDTO;
import com.shelfflow.dto.StaffLoginDTO;
import com.shelfflow.dto.StaffPageQueryDTO;
import com.shelfflow.entity.Staff;
import com.shelfflow.exception.AccountLockedException;
import com.shelfflow.exception.AccountNotFoundException;
import com.shelfflow.exception.PasswordErrorException;
import com.shelfflow.mapper.StaffMapper;
import com.shelfflow.result.PageResult;
import com.shelfflow.result.Result;
import com.shelfflow.service.StaffService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StaffServiceImpl implements StaffService {

    @Autowired
    private StaffMapper staffMapper;

    /**
     * 运营人员登录
     *
     * @param staffLoginDTO
     * @return
     */
    public Staff login(StaffLoginDTO staffLoginDTO) {
        String username = staffLoginDTO.getUsername();
        String password = staffLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Staff staff = staffMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (staff == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        password = DigestUtils.md5DigestAsHex(password.getBytes()); //hex:十六进制
        if (!password.equals(staff.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (staff.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return staff;
    }

    @Override
    public void add(StaffDTO staffDTO){
        Staff staff = new Staff();
        BeanUtils.copyProperties(staffDTO,staff);
        staff.setStatus(StatusConstant.ENABLE);
        staff.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
//        staff.setCreateTime(LocalDateTime.now());
//        staff.setUpdateTime(LocalDateTime.now());
//
//
//        staff.setCreateUser(CurrentActorContext.getCurrentId());
//        staff.setUpdateUser(CurrentActorContext.getCurrentId());

        staffMapper.add(staff);
    }


    public PageResult pageQuery(StaffPageQueryDTO staffPageQueryDTO){
        PageHelper.startPage(staffPageQueryDTO.getPage(),staffPageQueryDTO.getPageSize());
        Page<Staff> page = staffMapper.pageQuery(staffPageQueryDTO);
        Long total = page.getTotal();
        List<Staff> records = page.getResult();
        return new PageResult(total,records);
    }

    public void changeStatus(Integer status, Long id){
        Staff staff = Staff.builder().status(status).id(id).build();
        staffMapper.update(staff);
    }

    public Staff getById(Integer id){
        Staff staff =staffMapper.getById(id);
        staff.setPassword("******");
        return staff;
    }

    public void update(StaffDTO staffDTO){
        Staff staff = new Staff();
        BeanUtils.copyProperties(staffDTO,staff);
//        staff.setUpdateTime(LocalDateTime.now());
//        staff.setUpdateUser(CurrentActorContext.getCurrentId());
        staffMapper.update(staff);
    }
}
