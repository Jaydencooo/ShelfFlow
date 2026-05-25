package com.shelfflow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowTop10ReportVO implements Serializable {

    //商品名称列表，以逗号分隔，例如：临期酸奶,全麦吐司,轻食便当
    private String nameList;

    //流转量列表，以逗号分隔，例如：260,215,200
    private String numberList;

}
