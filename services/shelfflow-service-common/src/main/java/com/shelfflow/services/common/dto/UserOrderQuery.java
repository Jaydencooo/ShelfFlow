package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.api.PageQuery;
import lombok.Data;

@Data
public class UserOrderQuery extends PageQuery {
    private String status;
}
