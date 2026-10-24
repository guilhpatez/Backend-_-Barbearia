package com.barbshop.system.cloudflare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CloudflareResponse(

        @JsonProperty("success") boolean sucess,
        String hostname,
        String acao,
        @JsonProperty("error-codes") List<String> errorCode
        ){
}
