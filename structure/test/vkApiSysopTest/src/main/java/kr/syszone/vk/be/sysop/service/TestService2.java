package kr.syszone.vk.be.sysop.service;

import java.util.concurrent.CompletableFuture;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import kr.syszone.vk.be.db.HibernateUtil;
import kr.syszone.vk.be.db.entity.Company;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.CompanyApis;

@Path("/api/v1/sysop/test2")
public class TestService2 extends HttpService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CompanyService.class);
	
	private CompanyApis companyApis;
	
	@JsonRequest
	@Operation(summary = "COMPANY API - Endpoint Information")
	@GET
	@Path("/company")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on Company!!");
		if (companyApis == null) {
			companyApis = new CompanyApis();
		}
		CompletableFuture<CompanyApis> future = new CompletableFuture<>();
		future.complete(companyApis);
		return future;
	}
	
	
	@JsonRequest
	@Operation(summary = "COMPANY API - List")
	@GET
	@Path("/company/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<Company> getCompanyList(@Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<Company> future = new CompletableFuture<>();
		LOGGER.info("GET : /api/v1/sysop/test2/company/list");
		Session session = null;
		try {
			SessionFactory sf = HibernateUtil.getSessionFactory();
			session = sf.openSession();
			Query<Company> countQuery = session.createQuery("From Company WHERE id = :id ",Company.class).setParameter("id", "cg");
			Company companyEntity = countQuery.getSingleResult();
			future.complete(companyEntity);
			
		} catch (Exception e) {
			LOGGER.error("Db 오류", e);
		} finally {
			LOGGER.info("6) 세션 닫힘");
			if (session != null) {
				session.close();
			}
		}
		return future;
	}
}
