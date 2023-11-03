package kr.syszone.vk.be.sysop.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class FileDeleteHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileDeleteHelper.class);

	private static final String FILES_DEL_BASE;
	private static final String FILE_DEL_BASE;
	private static final Gson GSON;

	static {
		// Creates the json object which will manage the information received
		GsonBuilder builder = new GsonBuilder();

		// Register an adapter to manage the date types as long values
		builder.registerTypeAdapter(Timestamp.class, new JsonDeserializer<Timestamp>() {
			public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				return new Timestamp(json.getAsJsonPrimitive().getAsLong());
			}
		});

		GSON = builder.create();

		InetAddress address = null;
		try {
			address = InetAddress.getByName("vkbe-file-service.vk.svc.cluster.local");
			LOGGER.info("Address = {}", address);
		} catch (UnknownHostException ex) {
			LOGGER.error("K8S POD resolving is failed");
		}
		if (address == null) {
			FILES_DEL_BASE = "https://rnd.valuekeeper.ai/api/v1/file/deleteFiles";
			FILE_DEL_BASE = "https://rnd.valuekeeper.ai/api/v1/file/";
		} else {
			FILES_DEL_BASE = "http://vkbe-file-service.vk.svc.cluster.local/api/v1/file/deleteFiles";
			FILE_DEL_BASE = "http://vkbe-file-service.vk.svc.cluster.local/api/v1/file/";
		}

		LOGGER.info("FILES_DEL_BASE = {}", FILES_DEL_BASE);
		LOGGER.info("FILE_DEL_BASE = {}", FILE_DEL_BASE);
	}

	public static List<FileVo> deleteFiles(List<String> fileIds) {
		if (fileIds == null || fileIds.size() == 0) {
			return new ArrayList<FileVo>();
		}

		HttpURLConnection con = null;
		BufferedReader br = null;
		try {
			String params = String.join("&files=", fileIds);
			String urlWithParams = FILES_DEL_BASE + "?files=" + params;

			LOGGER.info("urlWithParams : {} ", urlWithParams);

			URL url = new URL(urlWithParams);

			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("DELETE");

			if (FILES_DEL_BASE.startsWith("https")) {
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
			}

			int responseCode = con.getResponseCode();

			LOGGER.info("responseCode : {} ", responseCode);
			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}

			Type fileVoListType = new TypeToken<ArrayList<FileVo>>() {
			}.getType();

			List<FileVo> delList = GSON.fromJson(br, fileVoListType);

			return delList;
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

		return null;
	}

	public static FileVo deleteFile(String fileId) {
		if (fileId == null || fileId.length() == 0) {
			return null;
		}

		HttpURLConnection con = null;
		BufferedReader br = null;
		try {
			String delUrl = FILE_DEL_BASE + "/" + fileId;

			URL url = new URL(delUrl);

			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("DELETE");

			if (FILE_DEL_BASE.startsWith("https")) {
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
			}

			int responseCode = con.getResponseCode();

			LOGGER.info("responseCode : {} ", responseCode);
			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}

			FileVo delFile = GSON.fromJson(br, FileVo.class);

			return delFile;
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

		return null;
	}

}
