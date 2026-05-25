package com.shelfflow.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class CartItemDTO implements Serializable {

    private Long productId;
    private Long bundleId;
    private Long batchId;
    private String productSpec;

}
