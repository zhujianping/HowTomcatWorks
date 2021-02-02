package com.zjp.ex04.startup;


import com.sun.tools.doclets.formats.html.markup.HtmlAttr;
import com.zjp.ex04.core.SimpleContainer;
import org.apache.catalina.Container;
import org.apache.catalina.connector.http.HttpConnector;

public class Bootstrap {
    public static void main(String[] args) {
        HttpConnector connector = new HttpConnector();
        SimpleContainer container = new SimpleContainer();
        connector.setContainer(container);
        try {
            connector.initialize();
            connector.start();
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
