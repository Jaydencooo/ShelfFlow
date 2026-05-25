package com.shelfflow.controller.admin;

import com.shelfflow.result.Result;
import com.shelfflow.service.AiOpsService;
import com.shelfflow.vo.AiOpsSuggestionVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/ai-ops")
@Slf4j
public class AiOpsController {

    @Autowired
    private AiOpsService aiOpsService;

    @GetMapping("/suggestions")
    @ApiOperation("AI 运营助手建议")
    public Result<List<AiOpsSuggestionVO>> suggestions() {
        log.info("查询 AI 运营助手建议");
        return Result.success(aiOpsService.suggestions());
    }
}
