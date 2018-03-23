package com.vcarecity.InterfaceDebugging;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

/**
 * Hello world!
 *
 */
public class InterfaceTest
{
	private static final Logger LOG = LogManager.getLogger(InterfaceTest.class);

	public static HttpClientContext context = null;

	public static CookieStore cookieStore = null;

	public static CloseableHttpClient httpClient = null;

	public static CloseableHttpResponse response = null;

	public static RequestConfig requestConfig = null;

	static
	{
		init();
	}

	private static void init()
	{
		context = HttpClientContext.create();
		cookieStore = new BasicCookieStore();
		// 配置超时时间（连接服务端超时1秒，请求数据返回超时2秒）
		requestConfig = RequestConfig.custom().setConnectTimeout(120000).setSocketTimeout(60000)
				.setConnectionRequestTimeout(60000).build();
		// 设置默认跳转以及存储cookie
		httpClient = HttpClientBuilder.create().setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
				.setRedirectStrategy(new DefaultRedirectStrategy()).setDefaultRequestConfig(requestConfig)
				.setDefaultCookieStore(cookieStore).build();
	}

	public static String postJson(String url, String jsonString) throws Exception
	{
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
		if (jsonString != null)
		{
			StringEntity reqEntity = new StringEntity(jsonString, "UTF-8");
			reqEntity.setContentType("text/json");
			httpPost.setEntity(reqEntity);
		}

		HttpClientContext context = HttpClientContext.create();
		response = httpClient.execute(httpPost, context);
		String recontext = "";
		try
		{
			cookieStore = context.getCookieStore();
			List<Cookie> cookies = cookieStore.getCookies();
			for (Cookie cookie : cookies)
			{
				LOG.debug("key:" + cookie.getName() + "  value:" + cookie.getValue());
				System.out.println("key:" + cookie.getName() + "  value:" + cookie.getValue());
			}
			HttpEntity resEntity = null;
			resEntity = response.getEntity();
			System.out.println("entity2:" + resEntity);
			recontext = EntityUtils.toString(resEntity, "UTF-8");
			System.out.println("返回的JSON数据:" + recontext);
		}
		finally
		{
			response.close();
		}
		return recontext;
	}

	public static void postJSONObect(String url, String jsonString) throws Exception
	{
		try
		{
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");

			// 解决中文乱码问题
			StringEntity stringEntity = new StringEntity(jsonString, "UTF-8");
			stringEntity.setContentEncoding("UTF-8");

			httpPost.setEntity(stringEntity);

			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>()
			{
				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException
				{//
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300)
					{

						HttpEntity entity = response.getEntity();

						return entity != null ? EntityUtils.toString(entity) : null;
					}
					else
					{
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};

			String responseBody = httpClient.execute(httpPost, responseHandler);

			System.out.println("----------------------------------------");
			System.out.println(responseBody);

		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	public static String call(String app_id, String app_key, String url, Map<String, Object> parameters)
			throws Exception
	{
		String jsonString = "";
		long time = System.currentTimeMillis();
		long random_number = Long.parseLong(RandomUtil.getFixLenthString(6));
		String prama = "?app_id=" + app_id + "&app_key=" + app_key + "&time=" + time + "&random_number=" + random_number
				+ "";
		String originalCode = "app_key=" + app_key + "&time=" + time + "&random_number=" + random_number + "";
		String check_code = MD5Util.getMD5(originalCode);
		prama = prama + "&check_code=" + check_code;
		JSONObject obj = new JSONObject();
		obj.put("app_id", app_id);
		obj.put("app_key", app_key);
		obj.put("time", time);
		obj.put("random_number", random_number);
		obj.put("check_code", check_code);
		obj.putAll(parameters);
		jsonString = InterfaceTest.postJson(url, obj.toString());
		return jsonString;
	}

	public static void main(String args[]) throws Exception
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("pageSize", 10);
		parameters.put("pageNum", 1);
		String ruselt = InterfaceTest.call("thisisatest", "vcarecity@0!7",
			"http://127.0.0.1:9090/v1/alarm_information/list", parameters);
		System.out.print(ruselt);

	}
}
