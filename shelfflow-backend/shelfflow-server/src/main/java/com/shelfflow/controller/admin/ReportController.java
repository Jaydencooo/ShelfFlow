package com.shelfflow.controller.admin;

import com.shelfflow.result.Result;
import com.shelfflow.service.ReportService;
import com.shelfflow.vo.FlowAmountReportVO;
import com.shelfflow.vo.FlowTop10ReportVO;
import com.shelfflow.vo.OrderReportVO;
import com.shelfflow.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "管理端-统计接口")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/flow-amount")
    @ApiOperation("流转额统计")
    public Result<FlowAmountReportVO> flowAmountStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end
            ) {
        log.info("流转额统计, begin:{}, end:{}", begin, end);
        FlowAmountReportVO flowAmountReportVO = reportService.getFlowAmount(begin, end);
        return Result.success(flowAmountReportVO);
    }

    @GetMapping("/userStatistics")
    @ApiOperation("用户数据统计")
    public Result<UserReportVO> UserStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("用户数据统计, begin:{}, end:{}", begin, end);
        return Result.success(reportService.getUser(begin, end));
    }

    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("订单统计：begin:{}, end:{}", begin, end);
        return Result.success(reportService.getOrders(begin, end));
    }

    @GetMapping("/top10")
    @ApiOperation("组合包+商品流转 Top10 统计")
    public Result<FlowTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("组合包+商品 top10统计：begin:{}, end:{}", begin, end);
        return Result.success(reportService.getTop10(begin, end));
    }
    @GetMapping("/export")
    @ApiOperation("导入运营分析xlsx表格")
    public void export(HttpServletResponse response){
//        log.info("导入运营分析xlsx表格");
        reportService.export(response);
//        return Result.success();
    }

}
