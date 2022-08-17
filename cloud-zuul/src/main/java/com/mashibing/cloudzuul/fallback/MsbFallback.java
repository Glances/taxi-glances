package com.mashibing.cloudzuul.fallback;

import com.mashibing.internalcommon.dto.ResponseResult;
import net.sf.json.JSONObject;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import javax.xml.ws.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author 马士兵教育:晁鹏飞
 * @date
 */
@Component
public class MsbFallback implements FallbackProvider {

    @Override
    public String getRoute() {
        return "*";
    }

    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.BAD_REQUEST;
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return HttpStatus.BAD_REQUEST.value();
            }

            @Override
            public String getStatusText() throws IOException {
                return "message ";
            }

            @Override
            public void close() {

            }

            @Override
            public InputStream getBody() throws IOException {
                // 通用的返回值 结构一样。{code:11,message:"",data:""}，方便前端处理
                // 降级的代码
                // {code:-1,message:"",data:"{orderStatus:-1(未知)}"}
                return new ByteArrayInputStream(JSONObject.fromObject(ResponseResult.fail(-100,"sd")).toString().getBytes());
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
            }
        };
    }
}
