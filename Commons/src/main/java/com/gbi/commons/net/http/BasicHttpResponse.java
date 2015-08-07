package com.gbi.commons.net.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

@Immutable
public class BasicHttpResponse {
	private String url = null;
	private Header[] headers = null;
	private byte[] content = null;
	private String contentType = null;
	private String contentCharset = null;

	public BasicHttpResponse(HttpClientContext context) throws IOException {
		HttpHost host = (HttpHost) context.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
		HttpRequest request = (HttpRequest) context.getAttribute(HttpClientContext.HTTP_REQUEST);
		String hostUri = host.toURI();
		String requestLine = request.getRequestLine().getUri();
		while (requestLine.startsWith(hostUri)) {
			requestLine = requestLine.substring(hostUri.length());
		}
		while (hostUri.endsWith("http")) {
			hostUri = hostUri.substring(0, hostUri.length() - 4);
		}
		url = hostUri + requestLine;
		HttpResponse response = (HttpResponse) context.getAttribute(HttpClientContext.HTTP_RESPONSE);
		headers = response.getAllHeaders();
		content = EntityUtils.toByteArray(response.getEntity());
		String temp = response.getEntity().getContentType().getValue();
		if (temp.indexOf(';') != -1) {
			contentType = temp.substring(0, temp.indexOf(';')).trim();
			contentCharset = temp.substring(temp.indexOf("=") + 1).trim();
		} else {
			contentType = temp.trim();
			if ("text/html".equals(contentType)) {
				contentCharset = searchContentCharset_html();
			} else if ("application/xml".equals(contentType)) {
				contentCharset = searchContentCharset_xml();
			} else if ("text/xml".equals(contentType)) {
				contentCharset = "US-ASCII";
			}
		}
	}

	public byte[] getContent() {
		return content;
	}

	public String getContentCharset() {
		return contentCharset;
	}

	public String getContentType() {
		return contentType;
	}

	/**
	 * @return a document of HTML or XML
	 */
	public Document getDocument() {
		return getDocument(null);
	}

	/**
	 * 
	 * @param charset
	 *            force the return document to use
	 * @return a document of HTML or XML
	 */
	public Document getDocument(String charset) {
		try {
			return Jsoup.parse(new String(content, charset == null ? contentCharset : charset), url,
					"text/html".equals(contentType) ? Parser.htmlParser() : Parser.xmlParser());
		} catch (UnsupportedEncodingException e) {
			System.err.println("incorrect charset: " + contentCharset);
			return null;
		}
	}

	public Header[] getHeaders() {
		return headers;
	}
	
	public String getMd5() {
		return DigestUtils.md5Hex(content);
	}

	public String getUrl() {
		return url;
	}

	private String searchContentCharset_html() throws UnsupportedEncodingException {
		String temp = new String(content, "ISO-8859-1");
		int index1 = temp.indexOf("<head>");
		int index2 = temp.indexOf("</head>");
		if (index1 != -1 && index2 != -1 && index1 < index2) {
			Document dom = Jsoup.parse(temp.substring(index1, index2 + "</head>".length()), "ISO-8859-1");
			Element meta = dom.select("meta[http-equiv=Content-Type][content*=charset]").first();
			if (meta != null) {
				temp = meta.attr("content");
				index1 = temp.indexOf("charset");
				index1 = temp.indexOf("=", index1);
				return temp.substring(index1 + 1).trim();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private String searchContentCharset_xml() throws UnsupportedEncodingException {
		String temp = new String(content, "ISO-8859-1");
		if (temp.indexOf("<?xml") == 0) {
			int index1 = temp.indexOf("encoding");
			int index2 = temp.indexOf("?>");
			if (index1 != -1 && index2 != -1 && index1 < index2) {
				index1 = temp.indexOf("\"", index1);
				index2 = temp.indexOf("\"", index1 + 1);
				if (index1 != -1 && index2 != -1 && index1 < index2) {
					return temp.substring(index1 + 1, index2);
				}
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		try {
			return new String(content, contentCharset == null ? "US-ASCII" : contentCharset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
