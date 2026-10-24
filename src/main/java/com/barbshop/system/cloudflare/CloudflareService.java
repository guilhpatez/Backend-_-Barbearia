package com.barbshop.system.cloudflare;

import com.barbshop.system.exception.ValidacaoCloudflare;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class CloudflareService {


    @Value("${CLOUDFLARE_SECRET_KEY}")
    private String tokenCloudflare;

    private static final String SITEVERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    private RestClient clientRest = RestClient.create();

    //Validação do token
    public boolean validar(String token, String ipCliente) {
        if (token == null || token.isBlank()) {
            return false;
        }

        MultiValueMap<String, String> corpo = new LinkedMultiValueMap<>();
        corpo.add("secret", tokenCloudflare);
        corpo.add("response", token);
        corpo.add("remoteip", ipCliente);

        try {
            CloudflareResponse resposta = clientRest.post()
                    .uri(SITEVERIFY_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(corpo)
                    .retrieve()
                    .body(CloudflareResponse.class);

            return resposta != null && resposta.sucess();
        } catch (RestClientException exception) {
            return false;
        }
    }

    public void validarOuFalhar(String token, String ipCliente) {
        if (!validar(token, ipCliente)) {
            throw new ValidacaoCloudflare("Verificação de segurança falhou. Tente novamente.");
        }
    }





}
