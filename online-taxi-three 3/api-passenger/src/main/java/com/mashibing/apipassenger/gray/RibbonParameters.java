package com.mashibing.apipassenger.gray;

import com.mashibing.internalcommon.dto.servicepassengeruser.T;
import org.springframework.stereotype.Component;

/**
 * @author 马士兵教育:晁鹏飞
 * @date 2020/07/02
 */
@Component
public class RibbonParameters {

    private static final ThreadLocal local = new ThreadLocal();

    // get
    public static <T> T get(){
        return  (T)local.get();
    }

    // set
    public static <T> void set(T t){
        local.set(t);
    }
}
