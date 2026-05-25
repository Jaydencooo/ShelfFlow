package com.shelfflow.controller.admin;

import com.shelfflow.result.Result;
import com.shelfflow.service.LossStatsService;
import com.shelfflow.vo.LossStatsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/loss-stats")
@Slf4j
@Api(tags = "损耗统计接口")
public class LossStatsController {

    @Autowired
    private LossStatsService lossStatsService;

    @GetMapping("/overview")
    @ApiOperation("损耗统计总览")
    public Result<LossStatsVO> overview() {
        log.info("损耗统计总览");
        return Result.success(lossStatsService.overview());
    }
}
