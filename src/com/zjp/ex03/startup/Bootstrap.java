package com.zjp.ex03.startup;

import com.zjp.ex03.connector.http.HttpConnector;

public class Bootstrap {
    public static void main(String[] args) {
        HttpConnector httpConnector = new HttpConnector();
        httpConnector.start();
    }
}
