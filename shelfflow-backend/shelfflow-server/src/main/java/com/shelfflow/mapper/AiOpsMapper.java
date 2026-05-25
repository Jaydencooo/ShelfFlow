package com.shelfflow.mapper;

import com.shelfflow.vo.AiOpsSuggestionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AiOpsMapper {

    List<AiOpsSuggestionVO> listSuggestions();
}
