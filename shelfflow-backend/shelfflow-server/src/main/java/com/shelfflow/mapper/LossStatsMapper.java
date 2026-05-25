package com.shelfflow.mapper;

import com.shelfflow.vo.LossStatsVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LossStatsMapper {

    LossStatsVO overview();
}
