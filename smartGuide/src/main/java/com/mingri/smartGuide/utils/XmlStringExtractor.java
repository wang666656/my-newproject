package com.mingri.smartGuide.utils;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageExtractor;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class XmlStringExtractor implements WebServiceMessageExtractor<String> {
    public static final XmlStringExtractor INSTANCE = new XmlStringExtractor();

    @Override
    public String extractData(WebServiceMessage message) {
        try {
            Source responseSource = message.getPayloadSource();
            StringWriter writer = new StringWriter();
            Result responseResult = new StreamResult(writer);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(responseSource, responseResult);

            return writer.toString();
        } catch (TransformerException e) {
            // 将检查异常转换为运行时异常，符合接口定义
            throw new RuntimeException("SOAP响应XML转换失败", e);
        }
    }
}