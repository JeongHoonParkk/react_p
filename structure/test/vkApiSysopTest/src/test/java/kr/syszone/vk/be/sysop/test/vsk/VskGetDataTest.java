package kr.syszone.vk.be.sysop.test.vsk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kr.syszone.vk.be.sysop.test.vsk.model.GnssData;
import kr.syszone.vk.be.sysop.test.vsk.model.GnssResponse;
import kr.syszone.vk.be.sysop.test.vsk.model.OrderData;
import kr.syszone.vk.be.sysop.test.vsk.model.OrderResponse;
import kr.syszone.vk.be.sysop.test.vsk.model.TempData;
import kr.syszone.vk.be.sysop.test.vsk.model.TempResponse;

public class VskGetDataTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(VskGetDataTest.class);

	private static String sid = ""; // This is set-Cookie at connect.sid=
	private static int orderCount = 0;
	private static int orderError = 0;
	private static List<String> errorList = new ArrayList<>();

	public VskGetDataTest() {
		// TODO Auto-generated constructor stub
	}

	public void getOrderList(String from, String to) {
		LOGGER.info("======getOrderList=======");
		String ORDERS_URL = "https://vsk.kr/api_tms/mediorder_ordersheet?center_id=&order_state=&car_id=&order_date=&deliver_date="
				+ "&order_date_between_from=" + from + "&order_date_between_to=" + to
				+ "&deliver_date_between_from=&deliver_date_between_to=";
		List<OrderData> result = new ArrayList<>();

		try {
			int responseCode = 0;
			URL url = new URL(ORDERS_URL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Cookie", sid);
			con.setDoInput(true);

			responseCode = con.getResponseCode();
			BufferedReader br;

			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
			}
			LOGGER.info("getOrderData responseCode : {}", responseCode);

			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}

			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
			OrderResponse rs = gson.fromJson(response.toString(), OrderResponse.class);

			for (OrderData od : rs.getData()) {
				LOGGER.info("Order List : {}", od);
				orderCount++;
				if (od.getOrderState().equals("Complete")) { // Packing is not ready!
					result.add(od);
				} else {
					errorList.add(od.getScanCode());
					orderError++;
				}
			}
			VskToVk vk = new VskToVk();
			vk.orderSave(result);
//			vk.orderUpdate(result);

			br.close();
			con.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("======getOrderList END=======");
		LOGGER.info("Completed order importing!! Count : " + orderCount + " ERROR : " + orderError
				+ " ERROR ORDER LIST : {}", errorList.toString());
	}

	public void getOrder(String orderNumber) {
		LOGGER.info("======getOrderData=======");
		String ORDER_URL = "https://vsk.kr/api_tms/mediorder_ordersheet?scan_code=" + orderNumber; // Get Order Info URL
		List<OrderData> result = new ArrayList<>();
		try {
			int responseCode = 0;
			URL url = new URL(ORDER_URL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Cookie", sid);
			con.setDoInput(true);

			responseCode = con.getResponseCode();
			BufferedReader br;

			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
			}
			LOGGER.info("getOrderData responseCode : {}", responseCode);

			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}

			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
			OrderResponse rs = gson.fromJson(response.toString(), OrderResponse.class);
			LOGGER.info("Order Data : {}", rs.getData()[0]);

			for (OrderData od : rs.getData()) {
				LOGGER.info("Order List : {}", od);
				orderCount++;
				if (od.getOrderState().equals("Complete")) { // Packing is not ready!
					result.add(od);
				} else {
					errorList.add(od.getScanCode());
					orderError++;
				}
			}

			VskToVk vk = new VskToVk();
			vk.orderSave(result);

			br.close();
			con.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("======getOrderData END=======");
	}

	public void getGnssData(String mac, String from, String to, String productId, String orderId) {

		LOGGER.info("======getGnssDate=======");

		List<GnssData> result = new ArrayList<>();

		try {
			String GNSS_URL = "https://vsk.kr/api_tms/datas_map_tracker?blemac=" + mac + "&from="
					+ URLEncoder.encode(from, "utf-8") + "&to=" + URLEncoder.encode(to, "utf-8"); // Get
			LOGGER.info(GNSS_URL);
			int responseCode = 0;
			URL url = new URL(GNSS_URL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Cookie", sid);
			con.setDoInput(true);

			responseCode = con.getResponseCode();
			BufferedReader br;

			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
			}
			LOGGER.info("getTempData responseCode : {}", responseCode);

			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			LOGGER.info("response : {}", response.toString());
			Gson gson = new Gson();
//			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
			GnssResponse rs = gson.fromJson(response.toString(), GnssResponse.class);
			LOGGER.info("GNSS Data : {}", rs.getData()[0]);
////
			for (GnssData od : rs.getData()) {
				LOGGER.info("GNSS : {}", od);
				result.add(od);
			}
			LOGGER.info("Result List Size : {}", result.size());
////
			VskToVk vk = new VskToVk();
			vk.initInflux();
			vk.influxGnssLog(result, mac, productId, orderId);

			br.close();
			con.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("======getGnssDate END=======");

	}

	public void getTempData(String mac, String from, String to, String productId, String orderId) {

		LOGGER.info("======getTempData=======");

		List<TempData> result = new ArrayList<>();
		try {
			String TEMP_URL = "https://vsk.kr/api_sensor/sdata/fromto_public/v2?blemac=" + mac + "&from="
					+ URLEncoder.encode(from, "utf-8") + "&to=" + URLEncoder.encode(to, "utf-8"); // Get
			LOGGER.info(TEMP_URL);
			int responseCode = 0;
			URL url = new URL(TEMP_URL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Cookie", sid);
			con.setDoInput(true);

			responseCode = con.getResponseCode();
			BufferedReader br;

			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
			}
			LOGGER.info("getTempData responseCode : {}", responseCode);

			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			LOGGER.info("response : {}", response.toString());
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
			TempResponse rs = gson.fromJson(response.toString(), TempResponse.class);
			LOGGER.info("Temp Data : {}", rs.getData()[0]);
//
			for (TempData od : rs.getData()) {
				LOGGER.info("Order List : {}", od);
				result.add(od);
			}
			LOGGER.info("Result List Size : {}", result.size());
//
			VskToVk vk = new VskToVk();
			vk.initInflux();
			vk.influxSensorLog(result, mac, productId, orderId);

			br.close();
			con.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("======getTempData END=======");

	}

	public void doLogout() {
		LOGGER.info("==========doLogout========");
		String LOGOUT_URL = "https://vsk.kr/api_auth/signout/v2";
		String body = "";
		try {
			int responseCode = 0;
			URL url = new URL(LOGOUT_URL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Cookie", sid);
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

	public void doLogin() {
		LOGGER.info("=========doLogin=======");

		String URL = "https://vsk.kr/api_auth/signin/v2";
		String LoginInfo = "{ \"account_id\" : \"gccell\", \"password\" : \"1234\", \"ver\" : \"mchainSkbs_v22.9.22\"}";
		BufferedReader br = null;
		try {

			int responseCode = 0;
			URL url = new URL(URL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);
			con.setDoInput(true);
			try (OutputStream os = con.getOutputStream()) {
				byte request_data[] = LoginInfo.getBytes("utf-8");
				os.write(request_data);
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			responseCode = con.getResponseCode();
			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}

			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();

			String cookies = "";
			String[] cookiesStr = null;
			Map<String, List<String>> header = con.getHeaderFields();
			if (header.containsKey("Set-Cookie")) {
				List<String> cookie = header.get("Set-Cookie");
				for (int i = 0; i < cookie.size(); i++) {
					cookies += cookie.get(i);
				}
				cookiesStr = cookies.split(";");
				for (String str : cookiesStr) {
					if (str.startsWith("connect.sid")) {
						System.out.println("Cookie :  " + str);
						sid = str;
					}
				}
			}

//			doLogout(sid);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		LOGGER.info("=========doLogin END=======");

	}

	public static void main(String[] args) {

		VskGetDataTest vsk = new VskGetDataTest();
		vsk.doLogin();
//		vsk.getGnssData("220620838752", "2022-01-26 14:35:38", "2022-01-26 15:16:38", "645", "424"); // GpsMac, StartTime, EndTime;
//		vsk.getTempData("C165DA65FA3C", "2022-09-16 09:58:39", "2022-09-16 11:54:20"); // TempSensor, StartTime, EndTime;
//		vsk.getOrder("2209229078");
//		vsk.getOrder("2209209078");
//		vsk.getOrderList("2022-09-01", "2022-09-02");
		vsk.getOrderList("2022-09-01", "2022-10-18");
		vsk.doLogout();

	}
}
