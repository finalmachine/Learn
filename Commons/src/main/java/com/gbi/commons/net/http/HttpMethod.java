package com.gbi.commons.net.http;

public enum HttpMethod {
	GET(false), POST(true);

    private final boolean hasBody;

    private HttpMethod(boolean hasBody) {
        this.hasBody = hasBody;
    }

    /**
     * Check if this HTTP method has/needs a request body
     * @return if body needed
     */
    public final boolean hasBody() {
        return hasBody;
    }
}
