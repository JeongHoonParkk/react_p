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
import kr.syszone.vk.be.sysop.model.SysopApis;

@Path("/api/v1")
public class SysopService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SysopService.class);

	private SysopApis sysopApiInfo;

	@JsonRequest
	@Operation(summary = "SYSOP API - Endpoint Information")
	@GET
	@Path("/sysop")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<SysopApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on SYSOP!!");
		if (sysopApiInfo == null) {
			sysopApiInfo = new SysopApis();
		}
		CompletableFuture<SysopApis> future = new CompletableFuture<>();
		future.complete(sysopApiInfo);
		return future;
	}

}
