package com.shelfflow.controller.admin;

import com.shelfflow.constant.JwtClaimsConstant;
import com.shelfflow.dto.StaffDTO;
import com.shelfflow.dto.StaffLoginDTO;
import com.shelfflow.dto.StaffPageQueryDTO;
import com.shelfflow.entity.Staff;
import com.shelfflow.properties.JwtProperties;
import com.shelfflow.result.PageResult;
import com.shelfflow.result.Result;
import com.shelfflow.service.StaffService;
import com.shelfflow.utils.JwtUtil;
import com.shelfflow.vo.StaffLoginVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 运营人员管理
 */
@RestController
@RequestMapping("/admin/staff")
@Slf4j
public class StaffController {

    @Autowired
    private StaffService staffService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param staffLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<StaffLoginVO> login(@RequestBody StaffLoginDTO staffLoginDTO) {
        log.info("运营人员登录：{}", staffLoginDTO);

        Staff staff = staffService.login(staffLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, staff.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        StaffLoginVO staffLoginVO = StaffLoginVO.builder()
                .id(staff.getId())
                .userName(staff.getUsername())
                .name(staff.getName())
                .token(token)
                .build();

        return Result.success(staffLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

//    新增运营人员
    @ApiOperation("新增运营人员")
    @PostMapping
    public Result add(@RequestBody StaffDTO staffDTO){
        log.info("新增运营人员：{}",staffDTO);
        staffService.add(staffDTO);
        return Result.success();
    }

    //根据运营人员姓名，分页查询
    @ApiOperation("根据运营人员姓名，分页查询")
    @GetMapping("/page")
    public Result<PageResult> pageQuery(StaffPageQueryDTO staffPageQueryDTO){
        log.info("根据运营人员姓名，分页查询。参数:{}",staffPageQueryDTO);
        PageResult pageResult = staffService.pageQuery(staffPageQueryDTO);
        return Result.success(pageResult);
    }

    //启用禁用运营人员账号
    @ApiOperation("启动禁用运营人员账号(根据id改变status）")
    @PostMapping("/status/{status}")
    public Result changeStatus(@PathVariable Integer status, Long id){
        log.info("启动禁用运营人员账号(根据id改变status）,id:{}, status:{}",id,status);
       staffService.changeStatus(status,id);
       return Result.success();
    }

    @ApiOperation("根据id查询运营人员(回显)")
    @GetMapping("/{id}")
    public Result<Staff> getById(@PathVariable Integer id){
        log.info("根据id查询运营人员，id：{}",id);
        Staff staff = staffService.getById(id);
        return Result.success(staff);
    }

    @ApiOperation("修改运营人员信息")
    @PutMapping
    public Result update(@RequestBody StaffDTO staffDTO){
        log.info("修改运营人员信息到：{}",staffDTO);
        staffService.update(staffDTO);
        return Result.success();
    }



}
