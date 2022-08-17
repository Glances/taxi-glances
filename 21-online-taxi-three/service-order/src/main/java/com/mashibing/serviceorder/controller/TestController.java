package com.mashibing.serviceorder.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 马士兵教育:chaopengfei
 * @date 2020/7/29
 */
@RestController
public class TestController {

    @GetMapping("")
    public String test(){

        return "order service";
    }
}
