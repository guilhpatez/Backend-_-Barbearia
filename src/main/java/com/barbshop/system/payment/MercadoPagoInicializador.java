package com.barbshop.system.payment;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
//Inicializador do SDK do Mercado Pago
public class MercadoPagoInicializador {

    //Chave API
    @Value("${MERCADO_PAGO_ACESS_KEY}")
    private String mercadoPagoAcessKey;

    @PostConstruct
    public void configurar() {
        MercadoPagoConfig.setAccessToken(mercadoPagoAcessKey);
    }

}
