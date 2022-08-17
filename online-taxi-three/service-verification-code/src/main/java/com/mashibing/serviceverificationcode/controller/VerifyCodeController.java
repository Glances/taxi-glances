package com.mashibing.serviceverificationcode.controller;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.serviceverificationcode.service.VerifyCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.GET;

@RestController
@RequestMapping("/verify-code")
public class VerifyCodeController {

    @Autowired
    VerifyCodeService verifyCodeService;

    @GetMapping("/generate/{identity}/{phoneNumber}")
    public ResponseResult generate(@PathVariable("identity") int identity , @PathVariable ("phoneNumber") String phoneNumber){


        return verifyCodeService.generate(identity,phoneNumber);
    }
}
