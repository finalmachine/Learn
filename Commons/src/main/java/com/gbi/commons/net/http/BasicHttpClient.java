package com.gbi.commons.net.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.*;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.RedirectException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class BasicHttpClient implements Closeable {

	// default config
	protected static final int defaultConnectTimeout = 60000; // 请求超时的时间
	protected static final int defaultSocketTimeout = 60000; // 响应超时的时间
	protected static final int setConnectionRequestTimeout = 60000;
	protected static final int defaultMaxRedirects = 3;

	// default headers
	protected static final String Accept = "text/html,application/xhtml+xml,application/xml;q=0.9,application/json;q=0.9,image/webp,*/*;q=0.8";
	protected static final String Accept_Charset = "utf-8, gbk;q=0.9";
	protected static final String Accept_Language = "zh-CN,zh;q=0.8";
	protected static final String User_Agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36";


	// default error code
	protected static final int ConnectTimeoutError = -1;
	protected static final int SocketTimeoutError = -2;
	protected static final int OverMaxRedirectsError = -3;
	protected static final int HttpHostConnectError = -4;
	protected static final int SocketExceptionError = -5; // 有可能是服务器忽然被关闭了
	protected static final int NoHttpResponseError = -6;
	protected static final int SSLHandshakeError = -7;
	protected static final int ProtocolError = -8;
	protected static final int ConnectionClosedError = -9; // 有可能是Content-Length 与 实际接收到的 不一致
	protected static final int SSLError = -10; // 多数情况是读取content过程中发生的错误


	private String proxystr;
	/**
	 * 重写验证方法，取消检测ssl
	 */
	private static TrustManager trustAllManager = new X509TrustManager() {
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	};

	protected CloseableHttpClient client = null;
	protected HttpClientContext context = null;
	protected HttpRequestBase request = null;
	protected CloseableHttpResponse response = null;
	protected RequestConfig config = null;
	protected HttpHost proxy = null;
	protected int lastStatus = 0;
	protected int networkTimeout = -1;

	/**
	 * 建立一个简单的
	 */
	public BasicHttpClient() {
		// 将HTTPS的网站证书设置成不检查的状态
		SSLContext sslcontext;
		try {
			sslcontext = SSLContexts.createDefault();
			sslcontext.init(null, new TrustManager[]{trustAllManager}, null);
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		}
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, null,
				null, NoopHostnameVerifier.INSTANCE);
		client = HttpClients.custom().setSSLSocketFactory(sslsf).setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).build();
		// 初始化context
		context = new HttpClientContext();
	}

	/**
	 * @param requestBase the HTTP request to be send
	 *                    Accept			text/html, application/xhtml+xml,
	 *                    application/xml;q=0.9, application/json;q=0.9,
	 *                    image/webp,
	 *                    other;q=0.8
	 *                    Accept-Charset	utf-8, gbk;q=0.9, iso-8859-1;q=0.8
	 *                    Accept-Encoding*	gzip, deflate
	 *                    Accept-Language	zh-CN,zh;q=0.8
	 *                    Connection*		keep-alive
	 *                    Host*			as you need
	 *                    User-Agent		Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36
	 */
	protected void setHeaders(HttpRequestBase requestBase, Map<String, String> extraHeaders) {
		requestBase.setHeader("Accept", Accept);
		requestBase.setHeader("Accept-Language", Accept_Language);
	//	requestBase.setHeader("Accept-Charset", Accept_Charset);
		requestBase.setHeader("User-Agent", User_Agent);
		if (extraHeaders != null) {
			for (String headName : extraHeaders.keySet()) {
				requestBase.setHeader(headName, extraHeaders.get(headName));
			}
		}
	}

	protected void prepare(HttpMethod method, String uri, Map<String, String> extraHeaders, Map<String, String> data) {
		switch (method) {
			case GET:
				request = new HttpGet(uri);
				break;
			case POST:
				HttpPost requestPost = new HttpPost(uri);
				List<NameValuePair> list = data.keySet().stream().map(key -> new BasicNameValuePair(key, data.get(key))).collect(Collectors.toList());
				try {
					requestPost.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				request = requestPost;
				break;
			default:
				break;
		}
		lastStatus = 0;
		setHeaders(request, extraHeaders);
		// 设置http请求的配置参数
		config = RequestConfig.custom()//
				.setMaxRedirects(defaultMaxRedirects)//
				.setSocketTimeout(networkTimeout == -1 ? defaultSocketTimeout : networkTimeout)//
				.setConnectTimeout(networkTimeout == -1 ? defaultConnectTimeout : networkTimeout)//
				.setConnectionRequestTimeout(networkTimeout == -1 ? setConnectionRequestTimeout : networkTimeout)//
				.setProxy(proxy)//
				.build();
		request.setConfig(config);
	}

	public BasicHttpResponse get(final String uri) {
		return get(uri, null, true);
	}

	public BasicHttpResponse get(final String uri, Map<String, String> extraHeaders) {
		return get(uri, extraHeaders, true);
	}

	public BasicHttpResponse get(final String uri, boolean onlySucessfulEntity) {
		return get(uri, null, onlySucessfulEntity);
	}

	public BasicHttpResponse get(final String uri, Map<String, String> extraHeaders, boolean onlySucessfulEntity) {
		prepare(HttpMethod.GET, uri, extraHeaders, null);
		try {
			if (!uri.startsWith("http://localhost"))
				System.out.println(proxystr + " " + uri + ">");
			response = client.execute(request, context);
			if (!uri.startsWith("http://localhost"))
				System.out.println(proxystr + " " + uri + "<");
			lastStatus = response.getStatusLine().getStatusCode();
			if (onlySucessfulEntity) {
				if (lastStatus / 100 != 2) {
					return null;
				}
			}
			BasicHttpResponse toReturn = new BasicHttpResponse(context);
			response.close();
			return toReturn;
		} catch (ConnectTimeoutException e) {
			lastStatus = ConnectTimeoutError;
		} catch (SocketTimeoutException e) {
			lastStatus = SocketTimeoutError;
		} catch (HttpHostConnectException e) {
			lastStatus = HttpHostConnectError;
		} catch (ClientProtocolException e) {
			if (e.getCause() instanceof RedirectException) {
				lastStatus = OverMaxRedirectsError;
			} else if (e.getCause() instanceof ProtocolException) {
				lastStatus = ProtocolError;
			} else {
				throw new RuntimeException(e);
			}
		} catch (SocketException e) {
			lastStatus = SocketExceptionError;
		} catch (NoHttpResponseException e) {
			lastStatus = NoHttpResponseError;
		} catch (SSLHandshakeException e) {
			lastStatus = SSLHandshakeError;
		} catch (SSLException e) {
			lastStatus = SSLError;
		} catch (ConnectionClosedException e) {
			lastStatus = ConnectionClosedError;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					System.err.println("response 关闭失败");
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public BasicHttpResponse post(final String uri, Map<String, String> data) {
		return post(uri, data, true);
	}

	public BasicHttpResponse post(final String uri, Map<String, String> data, boolean onlySucessfulEntity) {
		prepare(HttpMethod.POST, uri, null, data);
		try {
			response = client.execute(request, context);
			lastStatus = response.getStatusLine().getStatusCode();
			if (lastStatus == 302) {
				String url = response.getFirstHeader("Location").getValue();
				if (!url.startsWith("http")) {
					int i = uri.indexOf('/', 8);
					if (i == -1) {
						url = uri + url;
					} else {
						url = uri.substring(0, i) + url;
					}
				}
				response.close();
				return get(url, null, onlySucessfulEntity);
			} else {
				if (onlySucessfulEntity) {
					if (lastStatus / 100 != 2) {
						response.close();
						return null;
					}
				}
			}
			BasicHttpResponse toReturn = new BasicHttpResponse(context);
			response.close();
			return toReturn;
		} catch (ConnectTimeoutException e) {
			lastStatus = ConnectTimeoutError;
		} catch (SocketTimeoutException e) {
			lastStatus = SocketTimeoutError;
		} catch (HttpHostConnectException e) {
			lastStatus = HttpHostConnectError;
		} catch (ClientProtocolException e) {
			if (e.getCause() instanceof RedirectException) {
				lastStatus = OverMaxRedirectsError;
			} else if (e.getCause() instanceof ProtocolException) {
				lastStatus = ProtocolError;
			} else {
				throw new RuntimeException(e);
			}
		} catch (SocketException e) {
			lastStatus = SocketExceptionError;
		} catch (NoHttpResponseException e) {
			lastStatus = NoHttpResponseError;
		} catch (SSLHandshakeException e) {
			lastStatus = SSLHandshakeError;
		} catch (SSLException e) {
			lastStatus = SSLError;
		} catch (ConnectionClosedException e) {
			lastStatus = ConnectionClosedError;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					System.err.println("response 关闭失败");
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public CookieStore getCookieStore() {
		return context.getCookieStore();
	}

	/**
	 * 得到最后得到的HTTP状态码
	 *
	 * @return = -3} 超过最大重定向次数 = -2} Socket连接超时 = -1} Connect连接超时 = 0} 未设置 >= 1}
	 * HTTP协议规定的返回码
	 */
	public int getLastStatus() {
		return lastStatus;
	}

	/**
	 * Set proxy of the HttpClient by hostname and port.
	 *
	 * @param hostname the hostname (IP or DNS name)
	 * @param port     the port number.
	 *                 {@code -1} indicates the scheme default port.
	 */
	public void setProxy(String hostname, int port) {
		proxystr = hostname + ":" + port;
		proxy = new HttpHost(hostname, port);
	}

	/**
	 * Set proxy of the HttpClient by hostname and port.
	 *
	 * @param hostname the hostname (IP or DNS name)
	 * @param port     the port number.
	 *                 {@code -1} indicates the scheme default port.
	 */
	public void setProxy(String hostname, String port) {
		setProxy(hostname, Integer.parseInt(port));
	}

	public void removeCurrentProxy() {
		proxy = null;
	}

	/**
	 * @param millisecond equal or great than 0
	 */
	public void setNetworkTimeout(int millisecond) {
		networkTimeout = millisecond;
	}

	@Override
	public void close() {
		try {
			if (client != null) {
				client.close();
			}
		} catch (IOException e) {
			System.err.println("SimpleHttpClient close error");
		}
	}

	public static void main(String[] args) throws Exception {
		BasicHttpClient client = new BasicHttpClient();
		Map<String, String> data = new HashMap<>();
		data.put("message", "中文");
		BasicHttpResponse re = client.post("https://localhost:8443/WebTest/servlet/copy", data);
		System.out.println(client.getLastStatus());
		System.out.println(re.getDocument());
		client.close();
	}
}
