package com.shelfflow.service;

import com.shelfflow.vo.BusinessDataVO;
import com.shelfflow.vo.ProductOverviewVO;
import com.shelfflow.vo.OrderOverViewVO;
import com.shelfflow.vo.BundleOverviewVO;

import java.time.LocalDateTime;

public interface WorkspaceService {


    BusinessDataVO businessData(LocalDateTime beginTime, LocalDateTime endTime);

    OrderOverViewVO overviewOrders();

    ProductOverviewVO overviewProducts();

    BundleOverviewVO overviewBundles();
}
