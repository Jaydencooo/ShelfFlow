package com.shelfflow.service;

import com.shelfflow.vo.FlowAmountReportVO;
import com.shelfflow.vo.FlowTop10ReportVO;
import com.shelfflow.vo.OrderReportVO;
import com.shelfflow.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {

    FlowAmountReportVO getFlowAmount(LocalDate begin, LocalDate end);

    UserReportVO getUser(LocalDate begin, LocalDate end);

    OrderReportVO getOrders(LocalDate begin, LocalDate end);

    FlowTop10ReportVO getTop10(LocalDate begin, LocalDate end);

    void export(HttpServletResponse response);
}
