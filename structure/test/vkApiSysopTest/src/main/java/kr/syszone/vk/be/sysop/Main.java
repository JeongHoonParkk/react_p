package kr.syszone.vk.be.sysop;

import java.util.Arrays;
import java.util.HashSet;

import org.hibernate.annotations.common.util.impl.LoggerFactory;

import ch.qos.logback.classic.Logger;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import kr.syszone.vk.be.rest.HttpServer;
import kr.syszone.vk.be.rest.HttpServerBuilder;
import kr.syszone.vk.be.sysop.service.AirconInfoService;
import kr.syszone.vk.be.sysop.service.AirconService;
import kr.syszone.vk.be.sysop.service.CompanyGroupService;
import kr.syszone.vk.be.sysop.service.CompanyService;
import kr.syszone.vk.be.sysop.service.CompanyUserService;
import kr.syszone.vk.be.sysop.service.CompanyVehicleService;
import kr.syszone.vk.be.sysop.service.NoticeService;
import kr.syszone.vk.be.sysop.service.OrderDetailService;
import kr.syszone.vk.be.sysop.service.OrderService;
import kr.syszone.vk.be.sysop.service.OrderTBService;
import kr.syszone.vk.be.sysop.service.OrderTypeService;
import kr.syszone.vk.be.sysop.service.OrderWHService;
import kr.syszone.vk.be.sysop.service.PouchService;
import kr.syszone.vk.be.sysop.service.ProductHistoryService;
import kr.syszone.vk.be.sysop.service.ProductService;
import kr.syszone.vk.be.sysop.service.QnaService;
import kr.syszone.vk.be.sysop.service.SensorInfoService;
import kr.syszone.vk.be.sysop.service.SensorService;
import kr.syszone.vk.be.sysop.service.SysopService;
import kr.syszone.vk.be.sysop.service.TelegramService;
import kr.syszone.vk.be.sysop.service.TestService;
import kr.syszone.vk.be.sysop.service.TestService2;
import kr.syszone.vk.be.sysop.service.UserService;
import kr.syszone.vk.be.sysop.service.VehicleService;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		
		/*
		 * --------- CorsConfig ---------
		 */
		String[] origins = new String[] { "http://192.168.0.114:3000","http://httpbin.oauth2-proxy.localhost", "http://192.168.0.20:3000",
				"http://192.168.0.24:3000", "http://localhost:3000", "http://localhost", "http://59.9.223.3:31001",
				"http://182.162.169.212:31001", "http://182.162.169.213:31001", "http://182.162.169.214:31001" };
		CorsConfigBuilder builder = CorsConfigBuilder.forOrigins(origins);
		builder.allowedRequestHeaders(HttpHeaderNames.CONTENT_TYPE);
		builder.allowedRequestHeaders(HttpHeaderNames.CONTENT_LENGTH);
		builder.allowedRequestMethods(HttpMethod.POST, HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE,
				HttpMethod.HEAD, HttpMethod.OPTIONS);
		CorsConfig corsCfg = builder.build();

		/*
		 * --------- Swagger Info ---------
		 */
		Info info = new Info().title("ValueKeeper API Documentation").version("v1")
				.description("ValueKeeper - SYSOP API").contact(new Contact().email("cmjo@syszone.kr"))
				.license(new License().name("Apache License 2.0")
						.url("http://www.apache.org/licenses/LICENSE-2.0.html"));

		/*
		 * --------- Swagger Server ---------
		 */
		Server server = new Server();
		server.setUrl("http://localhost:18097/");

		/*
		 * --------- Swagger ---------
		 */
		OpenAPI swagger = new OpenAPI().info(info).addServersItem(server);

		/*
		 * --------- HttpServer (Netty) ---------
		 */
		HttpServer build = new HttpServerBuilder() // indent ----
				.setHttpServices(new HashSet<>(Arrays.asList(new SysopService(), new CompanyService(),
						new AirconInfoService(), new AirconService(), new SensorInfoService(), new SensorService(),
						new UserService(), new VehicleService(), new PouchService(), new CompanyGroupService(),
						new CompanyUserService(), new CompanyVehicleService(), new ProductService(),
						new ProductHistoryService(), new OrderService(), new OrderTypeService(), new OrderTBService(),
						new OrderWHService(), new OrderDetailService(), new NoticeService(), new QnaService(),
						new TelegramService(),new TestService(),new TestService2()))) // indent
				// ----
				.setUseEpollIfPossible(true) // indent ----
				.setCorsConfig(corsCfg) // indent ----
				.setApiJsonPath("/api/v1/sysop/swagger.json") // indent ----
				.setSwagger(swagger) // indent ----
				.build(); // indent ----

		/*
		 * --------- Bind HttpServer ---------
		 */
		build.bindAwait("0.0.0.0", 18097);
	}

}
