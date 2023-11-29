package org.springframework.xml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;

/**
 * @Version 1.0
 * @Author huqiang
 * @Description XmlParser
 * @Date 2023/11/29 11:47
 **/
public class XmlParser {
    private static final Logger logger = LogManager.getLogger(XmlParser.class);
    private XmlParser() {
    }

    public static String parse(String path) {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            SAXReader saxReader = SAXReader.createDefault();
            Document document = saxReader.read(inputStream);
            Element rootElement = document.getRootElement();
            Element element = rootElement.element("component-scan");
            Attribute basePackage = element.attribute("base-package");
            return basePackage.getText();
        } catch (Exception e) {
            logger.error("解析xml失败 {}",e.getMessage());
           throw new IllegalArgumentException("解析错误");
        }
    }
}