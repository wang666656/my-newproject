package com.mingri.smartGuide.utils;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapMessage;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

public class SoapRequestCallback implements WebServiceMessageCallback {

    private final String soapRequestXml;
    private final String soapAction;

    public SoapRequestCallback(String soapRequestXml, String soapAction) {
        this.soapRequestXml = soapRequestXml;
        this.soapAction = soapAction;
    }

    @Override
    public void doWithMessage(WebServiceMessage message) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            Source requestSource = new StreamSource(new StringReader(soapRequestXml));
            Result messageResult = message.getPayloadResult();
            transformer.transform(requestSource, messageResult);

            if (message instanceof SoapMessage) {
                ((SoapMessage) message).setSoapAction(soapAction);
            }
        } catch (TransformerException e) {
            // 将检查异常转换为运行时异常，符合接口定义
            throw new RuntimeException("SOAP请求实时排班数据异常", e);
        }
    }
}