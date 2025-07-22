package com.mingri.smartGuide.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.client.core.WebServiceTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // 配置 JAXB Marshaller，指定需要序列化的类包路径
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // 指定包含 @XmlRootElement 注解的类所在的包（根据实际项目调整）
        marshaller.setPackagesToScan("com.mingri.smartGuide.entity");
        return marshaller;
    }

    // 配置 WebServiceTemplate，并注入 Marshaller 和 Unmarshaller
    @Bean
    public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller) {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMarshaller(marshaller);
        webServiceTemplate.setUnmarshaller(marshaller);
        return webServiceTemplate;
    }
}