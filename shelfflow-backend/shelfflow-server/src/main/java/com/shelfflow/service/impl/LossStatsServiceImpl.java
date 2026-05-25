package com.shelfflow.service.impl;

import com.shelfflow.mapper.LossStatsMapper;
import com.shelfflow.service.LossStatsService;
import com.shelfflow.vo.LossStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LossStatsServiceImpl implements LossStatsService {

    @Autowired
    private LossStatsMapper lossStatsMapper;

    @Override
    public LossStatsVO overview() {
        return lossStatsMapper.overview();
    }
}
