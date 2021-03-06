package org.apache.http.impl.execchain;

import java.io.IOException;

import com.gbi.commons.config.Params;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.NonRepeatableRequestException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.util.Args;

/**
 * Request executor in the request execution chain that is responsible
 * for making a decision whether a request failed due to an I/O error
 * should be re-executed.
 * <p>
 * Further responsibilities such as communication with the opposite
 * endpoint is delegated to the next executor in the request execution
 * chain.
 * </p>
 *
 * @since 4.3
 */
@Immutable
public class RetryExec implements ClientExecChain {

	private final Log log = LogFactory.getLog(getClass());

	private final ClientExecChain requestExecutor;
	private final HttpRequestRetryHandler retryHandler;

	public RetryExec(
			final ClientExecChain requestExecutor,
			final HttpRequestRetryHandler retryHandler) {
		Args.notNull(requestExecutor, "HTTP request executor");
		Args.notNull(retryHandler, "HTTP request retry handler");
		this.requestExecutor = requestExecutor;
		this.retryHandler = retryHandler;
	}

	@Override
	public CloseableHttpResponse execute(
			final HttpRoute route,
			final HttpRequestWrapper request,
			final HttpClientContext context,
			final HttpExecutionAware execAware) throws IOException, HttpException {
		Args.notNull(route, "HTTP route");
		Args.notNull(request, "HTTP request");
		Args.notNull(context, "HTTP context");
		final Header[] origheaders = request.getAllHeaders();
		for (int execCount = 1;; execCount++) {
			try {
				Params.log.info(execCount + " ~ " + route.getProxyHost());
				return this.requestExecutor.execute(route, request, context, execAware);
			} catch (final IOException ex) {
				if (execAware != null && execAware.isAborted()) {
					Params.log.info(execCount + " ~ " + route.getProxyHost() + " ioe");
					this.log.debug("Request has been aborted");
					throw ex;
				}
				if (retryHandler.retryRequest(ex, execCount, context)) {
					if (this.log.isInfoEnabled()) {
						this.log.info("I/O exception ("+ ex.getClass().getName() +
								") caught when processing request to "
								+ route +
								": "
								+ ex.getMessage());
					}
					if (this.log.isDebugEnabled()) {
						this.log.debug(ex.getMessage(), ex);
					}
					if (!RequestEntityProxy.isRepeatable(request)) {
						this.log.debug("Cannot retry non-repeatable request");
						throw new NonRepeatableRequestException("Cannot retry request " +
								"with a non-repeatable request entity", ex);
					}
					request.setHeaders(origheaders);
					if (this.log.isInfoEnabled()) {
						this.log.info("Retrying request to " + route);
					}
				} else {
					Params.log.info(execCount + " ~ " + route.getProxyHost() + " in else");
					if (ex instanceof NoHttpResponseException) {
						final NoHttpResponseException updatedex = new NoHttpResponseException(
								route.getTargetHost().toHostString() + " failed to respond");
						updatedex.setStackTrace(ex.getStackTrace());
						throw updatedex;
					} else {
						throw ex;
					}
				}
			}
		}
	}

}