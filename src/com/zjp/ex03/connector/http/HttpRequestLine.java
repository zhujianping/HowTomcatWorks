package com.zjp.ex03.connector.http;

final class HttpRequestLine {
    public static final int INITIAL_METHOD_SIZE = 8;
    public static final int INITIAL_URI_SIZE = 64;
    public static final int INITIAL_PROTOCOL_SIZE = 8;
    public static final int MAX_METHOD_SIZE = 1024;
    public static final int MAX_URI_SIZE = 32768;
    public static final int MAX_PROTOCOL_SIZE = 1024;
    public char[] method;
    public int methodEnd;
    public char[] uri;
    public int uriEnd;
    public char[] protocol;
    public int protocolEnd;

    public HttpRequestLine() {
        this(new char[8], 0, new char[64], 0, new char[8], 0);
    }

    public HttpRequestLine(char[] method, int methodEnd, char[] uri, int uriEnd, char[] protocol, int protocolEnd) {
        this.method = method;
        this.methodEnd = methodEnd;
        this.uri = uri;
        this.uriEnd = uriEnd;
        this.protocol = protocol;
        this.protocolEnd = protocolEnd;
    }

    public void recycle() {
        this.methodEnd = 0;
        this.uriEnd = 0;
        this.protocolEnd = 0;
    }

    public int indexOf(char[] buf) {
        return this.indexOf(buf, buf.length);
    }

    public int indexOf(char[] buf, int end) {
        char firstChar = buf[0];

        for(int pos = 0; pos < this.uriEnd; ++pos) {
            pos = this.indexOf(firstChar, pos);
            if (pos == -1) {
                return -1;
            }

            if (this.uriEnd - pos < end) {
                return -1;
            }

            for(int i = 0; i < end && this.uri[i + pos] == buf[i]; ++i) {
                if (i == end - 1) {
                    return pos;
                }
            }
        }

        return -1;
    }

    public int indexOf(String str) {
        return this.indexOf(str.toCharArray(), str.length());
    }

    public int indexOf(char c, int start) {
        for(int i = start; i < this.uriEnd; ++i) {
            if (this.uri[i] == c) {
                return i;
            }
        }

        return -1;
    }

    public int hashCode() {
        return 0;
    }

    public boolean equals(Object obj) {
        return false;
    }
}
