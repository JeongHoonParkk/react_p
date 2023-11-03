package kr.syszone.vk.be.sysop.service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;

@Path("/api/v1/sysop")
public class TelegramService extends HttpService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TelegramService.class);

	private final String TELEGRAM_ENTER = "%0A";
	private final String TOKEN = "5421351956:AAEId5y5Z4vDC8E7S35_Y2pER9QfhVGIh6M";
	private final String CHAT_ID = "-1001525830121";

	@JsonRequest
	@Operation(summary = "SEND Telegram message")
	@POST
	@Path("/sendAlert")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<Map> sendAlert(@Parameter(hidden = true) RakamHttpRequest request, @RequestBody Map body) {
		LOGGER.info("/api/v1/tb/sendAlert");

		CompletableFuture<Map> future = new CompletableFuture<>();
		HttpURLConnection connection = null;
		try {
			LOGGER.info("1) Create Send Message !!!!!!!!");
			Map result = new HashMap();

			String status = (body!=null)?(body.get("status")!=null)?(String) body.get("status"):"":"";
			String location = (body!=null)?(body.get("location")!=null)?(String) body.get("location"):"":"";
			String date = (body!=null)?(body.get("date")!=null)?(String) body.get("date"):"":"";
			String detail = (body!=null)?(body.get("detail")!=null)?(String) body.get("detail"):"":"";

			LOGGER.info("2)  Send Message !!!!!!!!");

			String TELEGRAM_TEXT = ("{상태} 발생").replace("{상태}", status);
			TELEGRAM_TEXT += TELEGRAM_ENTER+(("{위치}에서 {상태}이 발생하였습니다.").replace("{위치}", location)).replace("{상태}", status);
			TELEGRAM_TEXT += TELEGRAM_ENTER+TELEGRAM_ENTER+"상태에 대한 내용은 다음과 같습니다.";
			TELEGRAM_TEXT += TELEGRAM_ENTER+("일시 : {일시}").replace("{일시}", date);
			TELEGRAM_TEXT += TELEGRAM_ENTER+("상세 내용 : {상세내용}").replace("{상세내용}", detail);

			LOGGER.info("{}", TELEGRAM_TEXT);
			String urlAddress = String.format("https://api.telegram.org/bot%s/sendmessage?chat_id=%s&text=%s", TOKEN, CHAT_ID, TELEGRAM_TEXT);
			LOGGER.info("urlAddress={}", urlAddress);

			URL url = new URL(urlAddress);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			int responseCode = connection.getResponseCode();

			result.put("sendText", TELEGRAM_TEXT.replaceAll(TELEGRAM_ENTER, "\r\n"));
			result.put("responseCode", responseCode);
			LOGGER.info("responseCode={}", responseCode);

			LOGGER.info("3) Return List !!!!!!!!!!!!!!!!!!!");
			future.complete(result);
		} catch (Exception e) {
			LOGGER.error("Send Telegram Message Failed !!!!!!!!!!!!!", e);
		} finally {
			if(connection != null) {
				connection.disconnect();
			}
		}
		return future;
	}

}
