package com.shelfflow.service.impl;

import com.shelfflow.mapper.AiOpsMapper;
import com.shelfflow.service.AiOpsService;
import com.shelfflow.vo.AiOpsSuggestionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiOpsServiceImpl implements AiOpsService {

    @Autowired
    private AiOpsMapper aiOpsMapper;

    @Override
    public List<AiOpsSuggestionVO> suggestions() {
        return aiOpsMapper.listSuggestions();
    }
}
