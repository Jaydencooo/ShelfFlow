package com.shelfflow.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class OrderStatisticsVO implements Serializable {
    //待接单数量
    private Integer toBeConfirmed;

    //备货中数量
    private Integer confirmed;

    //待自提数量
    private Integer readyForPickup;
}
