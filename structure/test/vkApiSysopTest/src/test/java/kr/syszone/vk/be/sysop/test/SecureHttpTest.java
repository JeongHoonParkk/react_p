package kr.syszone.vk.be.sysop.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureHttpTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecureHttpTest.class);

	private static final String FILES_DEL_BASE = "https://rnd.valuekeeper.ai/api/v1/file/deleteFiles";
//	private static final String FILE_DEL_BASE = "https://rnd.valuekeeper.ai/api/v1/file/";

	public static final int TIMEOUT_CONNECT = 15000;
	public static final int TIMEOUT_READ = 15000;

	public SecureHttpTest() {

	}

	@Test
	public void doTest() {

		HttpURLConnection con = null;
		BufferedReader br = null;

		try {
//			String params = String.join("&files=", 1);
			String urlWithParams = FILES_DEL_BASE + "?files=123";

			LOGGER.info("urlWithParams : {} ", urlWithParams);

			URL url = new URL(urlWithParams);


			con = (HttpURLConnection) url.openConnection();
			con.setReadTimeout(TIMEOUT_READ);
			con.setConnectTimeout(TIMEOUT_CONNECT);
			con.setRequestMethod("DELETE");
			con.setDoInput(true);

			LOGGER.info("HttpsURLConnection : openConnection()");

			if (true) {
				HttpsURLConnection sconn = (HttpsURLConnection) con;

				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					public void checkClientTrusted(X509Certificate[] certs, String authType) {
					}

					public void checkServerTrusted(X509Certificate[] certs, String authType) {
					}
				} };

				SSLContext sc = SSLContext.getInstance("TLS");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				sconn.setSSLSocketFactory(sc.getSocketFactory());

				sconn.setHostnameVerifier(
						new CustomHostnameVerifier(new String[] { "www.valuekeeper.ai", "rnd.valuekeeper.ai" }));
			}

			int responseCode = con.getResponseCode();

			LOGGER.info("responseCode : {} ", responseCode);

		} catch (Exception e) {
			LOGGER.error("Exception is occurred!!", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				con.disconnect();
			}
		}
	}

	private class CustomHostnameVerifier implements HostnameVerifier {

		private List<String> allowedHosts;

		public CustomHostnameVerifier(String[] allowedHosts) {
			this.allowedHosts = Arrays.asList(allowedHosts);
		}

		public boolean verify(String hostname, SSLSession session) {
			if (allowedHosts.contains(hostname)) {
				LOGGER.info("verify called!!! hostname = {}", hostname);
				return true;
			}
			return false;
		}
	}

	public static void main(String[] args) {
		SecureHttpTest sht = new SecureHttpTest();
		sht.doTest();
	}

}
