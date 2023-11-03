package kr.syszone.vk.be.sysop.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.OrderApis;

@Path("/api/v1/sysop")
public class OrderDetailService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderDetailService.class);

	private OrderApis orderApis;

	@JsonRequest
	@Operation(summary = "Order API - Endpoint Information")
	@GET
	@Path("/orderdetail")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<OrderApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on Order!!");
		if (orderApis == null) {
			orderApis = new OrderApis();
		}
		CompletableFuture<OrderApis> future = new CompletableFuture<>();
		future.complete(orderApis);
		return future;
	}



}
