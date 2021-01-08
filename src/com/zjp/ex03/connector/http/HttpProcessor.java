package com.zjp.ex03.connector.http;


import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.StringManager;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.ServerException;

public class HttpProcessor {
    private HttpConnector httpConnector;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private HttpRequestLine requestLine = new HttpRequestLine();
    protected String method = null;
    protected String queryString = null;

    protected StringManager sm = StringManager
            .getManager("com.zjp.ex03.connector.http");


    public HttpProcessor(HttpConnector httpConnector) {
        this.httpConnector = httpConnector;
    }

    public void process(Socket socket) {
        SocketInputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new SocketInputStream(socket.getInputStream(), 2048);
            outputStream = socket.getOutputStream();
            httpRequest = new HttpRequest(inputStream);
            httpResponse = new HttpResponse(outputStream);
            httpResponse.setRequest(httpRequest);
            httpResponse.setHeader("Server","Pyrmont servlet Container");

            parseRequest(inputStream,outputStream);
            parseHeaders(inputStream);

            if (httpRequest.getRequestURI().startsWith("/servlet")) {
                ServletProcessor servletProcessor = new ServletProcessor();
                servletProcessor.process(httpRequest,httpResponse);
            } else {
                StaticResourceProcessor staticResourceProcessor = new StaticResourceProcessor();
                staticResourceProcessor.process(httpRequest,httpResponse);
            }

            socket.close();
        } catch (IOException | ServletException e) {
            e.printStackTrace();
        }
    }

    private void parseHeaders(SocketInputStream input) throws IOException, ServletException {
        while (true) {
            HttpHeader header = new HttpHeader();
            ;

            // Read the next header
            input.readHeader(header);
            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    return;
                } else {
                    throw new ServletException(
                            sm.getString("httpProcessor.parseHeaders.colon"));
                }
            }

            String name = new String(header.name, 0, header.nameEnd);
            String value = new String(header.value, 0, header.valueEnd);
            httpRequest.addHeader(name, value);
            // do something for some headers, ignore others.
            if (name.equals("cookie")) {
                Cookie cookies[] = RequestUtil.parseCookieHeader(value);
                for (int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals("jsessionid")) {
                        // Override anything requested in the URL
                        if (!httpRequest.isRequestedSessionIdFromCookie()) {
                            // Accept only the first session id cookie
                            httpRequest.setRequestedSessionId(cookies[i].getValue());
                            httpRequest.setRequestedSessionCookie(true);
                            httpRequest.setRequestedSessionURL(false);
                        }
                    }
                    httpRequest.addCookie(cookies[i]);
                }
            } else if (name.equals("content-length")) {
                int n = -1;
                try {
                    n = Integer.parseInt(value);
                } catch (Exception e) {
                    throw new ServletException(
                            sm.getString("httpProcessor.parseHeaders.contentLength"));
                }
                httpRequest.setContentLength(n);
            } else if (name.equals("content-type")) {
                httpRequest.setContentType(value);
            }
        } // end while
    }

    private String normalize(String path) {
        if (path == null)
            return null;
        // Create a place for the normalized path
        String normalized = path;

        // Normalize "/%7E" and "/%7e" at the beginning to "/~"
        if (normalized.startsWith("/%7E") || normalized.startsWith("/%7e"))
            normalized = "/~" + normalized.substring(4);

        // Prevent encoding '%', '/', '.' and '\', which are special reserved
        // characters
        if ((normalized.indexOf("%25") >= 0)
                || (normalized.indexOf("%2F") >= 0)
                || (normalized.indexOf("%2E") >= 0)
                || (normalized.indexOf("%5C") >= 0)
                || (normalized.indexOf("%2f") >= 0)
                || (normalized.indexOf("%2e") >= 0)
                || (normalized.indexOf("%5c") >= 0)) {
            return null;
        }

        if (normalized.equals("/."))
            return "/";

        // Normalize the slashes and add leading slash if necessary
        if (normalized.indexOf('\\') >= 0)
            normalized = normalized.replace('\\', '/');
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index)
                    + normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index)
                    + normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null); // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2)
                    + normalized.substring(index + 3);
        }

        // Declare occurrences of "/..." (three or more dots) to be invalid
        // (on some Windows platforms this walks the directory tree!!!)
        if (normalized.indexOf("/...") >= 0)
            return (null);

        // Return the normalized path that we have completed
        return (normalized);

    }


    private void parseRequest(SocketInputStream inputStream, OutputStream outputStream) throws IOException {
        inputStream.readRequestLine(requestLine);
        String method = new String(requestLine.method, 0, requestLine.methodEnd);
        String uri = null;
        String protocol = new String(requestLine.protocol, 0, requestLine.protocolEnd);

        if (method.length() < 1) {
            throw new ServerException("Missing HTTP request method");
        } else if (requestLine.uriEnd < 1) {
            throw new ServerException("Missing HTTP request URI");
        }
        int question = requestLine.indexOf("?");
        if (question >= 0) {
            httpRequest.setQueryString(new String(requestLine.uri, question + 1, requestLine.uriEnd - question -1));
            uri = new String(requestLine.uri, 0, question);
        } else {
            httpRequest.setQueryString(null);
            uri = new String(requestLine.uri, 0, requestLine.uriEnd);
        }

        if (!uri.startsWith("/")) {
            int pos = uri.indexOf("://");
            //Parsing out protocol and host name
            if (pos != -1) {
                pos = uri.indexOf('/', pos + 3);
                if (pos == -1) {
                    uri = "";
                } else {
                    uri = uri.substring(pos);
                }
            }
        }
        String match = ":jsessionid=";
        int semicolon = uri.indexOf(match);
        if (semicolon >= 0) {
            String rest = uri.substring(semicolon + match.length());
            int semicolon2 = rest.indexOf(";");
            if (semicolon2 >= 0) {
                httpRequest.setRequestedSessionId(rest.substring(0, semicolon2));
                rest = rest.substring(semicolon2);
            } else {
                httpRequest.setRequestedSessionId(rest);
                rest = "";
            }
            httpRequest.setRequestedSessionURL(true);
            uri = uri.substring(0,semicolon) + rest;
        } else {
            httpRequest.setRequestedSessionId(null);
            httpRequest.setRequestedSessionURL(false);
        }

        String normailzedUri = normalize(uri);
        httpRequest.setMethod(method);
        httpRequest.setProtocol(protocol);

        if (normailzedUri != null) {
            httpRequest.setRequestURI(normailzedUri);
        } else {
            httpRequest.setRequestURI(uri);
        }

        if (normailzedUri == null) {
            throw new ServerException("Invalid URI: " + uri);
        }
    }


}
