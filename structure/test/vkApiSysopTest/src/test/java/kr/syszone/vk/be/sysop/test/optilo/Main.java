package kr.syszone.vk.be.sysop.test.optilo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private String JSESSIONID = null; // This is set-Cookie at JSESSIONID

	public boolean isLogin() {
		return (JSESSIONID != null);
	}

	public void doLogin() {
		LOGGER.info("=========doLogin=======");

		String URL = "http://green.thermocert.net/login";
		String LoginInfo = "user_id=green&user_pwd=green123";
		BufferedReader br = null;
		try {
			byte request_data[] = LoginInfo.getBytes("utf-8");

			int responseCode = 0;
			URL url = new URL(URL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
//			con.setRequestProperty("Content-Length", "" + request_data.length);
			con.setDoOutput(true);
			con.setDoInput(true);

			OutputStream os = null;
			try {
				os = con.getOutputStream();
				os.write(request_data);
			} catch (Exception e) {
				e.printStackTrace();
			}

			responseCode = con.getResponseCode();
			LOGGER.info("responseCode : {}", responseCode);
			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}

			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line).append('\n');
			}

			if (os != null) {
				os.close();
			}
			br.close();

			if ("fail".equals(sb.toString())) {
				LOGGER.info("Login is " + sb.toString());
				return;
			}

			Map<String, List<String>> header = con.getHeaderFields();
			if (header.containsKey("Set-Cookie")) {
				List<String> cookie = header.get("Set-Cookie");
				JSESSIONID = "";
				for (int i = 0; i < cookie.size(); i++) {
					JSESSIONID += cookie.get(i);
				}
			}

			LOGGER.info("Cookie : " + JSESSIONID);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		LOGGER.info("=========doLogin END=======");

	}

	public void doLogout() {
		LOGGER.info("==========doLogout========");
		String LOGOUT_URL = "http://green.thermocert.net/logout";
		String body = "";
		try {
			int responseCode = 0;
			URL url = new URL(LOGOUT_URL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("JSEESIONID", JSESSIONID);
			con.setDoOutput(true);
			con.setDoInput(true);
			try (OutputStream os = con.getOutputStream()) {
				byte request_data[] = body.getBytes("utf-8");
				os.write(request_data);
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			responseCode = con.getResponseCode();
			BufferedReader br;

			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
			}
			LOGGER.info("LOGOUT res : {}", responseCode);
			br.close();
			con.disconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOGGER.info("==========doLogout END========");
	}

	public void getDelivery(String httpURL) {
		LOGGER.info("=========getDelivery=======");

		BufferedReader br = null;
		try {

			int responseCode = 0;
			URL url = new URL(httpURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Cookie", JSESSIONID);

//			con.setDoOutput(true);
			con.setDoInput(true);
//			try (OutputStream os = con.getOutputStream()) {
//				byte request_data[] = LoginInfo.getBytes("utf-8");
//				os.write(request_data);
//				os.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}

			responseCode = con.getResponseCode();
			LOGGER.info("responseCode : {}", responseCode);
			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}

			String line;
			StringBuilder response = new StringBuilder();
			while ((line = br.readLine()) != null) {
				response.append(line).append('\n');
			}
			br.close();

			LOGGER.info("response : {}", response.toString());

//			doLogout(sid);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		LOGGER.info("=========doLogin END=======");
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Main main = new Main();
		main.doLogin();
		if (!main.isLogin()) {
			LOGGER.info("Login is failed!!");
			return;
		}
		main.getDelivery(
				"http://green.thermocert.net/detailInfo?de_number=SENSOR_1903B4&sdate=2022-09-29+06%3a00&edate=2022-09-30+00%3a00&type=vehicle&no=6082");
//		main.doLogout();
	}

}
