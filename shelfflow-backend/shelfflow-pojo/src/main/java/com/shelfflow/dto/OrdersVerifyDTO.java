package com.shelfflow.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersVerifyDTO implements Serializable {

    private Long orderId;

    private String pickupCode;
}
