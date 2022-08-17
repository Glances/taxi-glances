package com.mashibing.serviceverificationcode.service;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.dto.serviceverificationcode.VerifyCodeResponse;

public interface VerifyCodeService {

    public ResponseResult<VerifyCodeResponse> generate(int identity , String phoneNumber);
}
