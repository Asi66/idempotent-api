package com.asi.idempotent.api;

import com.asi.idempotent.application.UpdateTestCommand;
import com.asi.idempotent.infra.resubmit.Resubmit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author asi
 * @date 2023/10/7 15:50
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    @PostMapping("/update")
    @Resubmit
    public String update(@RequestBody UpdateTestCommand command){
        log.info("修改参数：{}",command.toString());
        return "SUCCESS";
    }
}
