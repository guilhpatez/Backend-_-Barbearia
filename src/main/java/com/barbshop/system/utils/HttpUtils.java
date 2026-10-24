package com.barbshop.system.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class HttpUtils {

    public String ipCliente (HttpServletRequest requestHttp) {
        return requestHttp.getRemoteAddr();
    }
}
