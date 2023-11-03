package kr.syszone.vk.be.sysop.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import kr.syszone.vk.be.db.HibernateUtil;
import kr.syszone.vk.be.db.entity.OrderType;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.OrderTypeApis;

@Path("/api/v1/sysop")
public class OrderTypeService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderTypeService.class);

	private OrderTypeApis orderTypeApis;

	@JsonRequest
	@Operation(summary = "Order Type API - Endpoint Information")
	@GET
	@Path("/ordertype")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<OrderTypeApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on Order!!");
		if (orderTypeApis == null) {
			orderTypeApis = new OrderTypeApis();
		}
		CompletableFuture<OrderTypeApis> future = new CompletableFuture<>();
		future.complete(orderTypeApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "Order Type API - Company Select List")
	@GET
	@Path("/ordertypes")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<OrderType>> getSelectCompanyList(
			@Parameter(hidden = true) RakamHttpRequest request) {

		CompletableFuture<List<OrderType>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/ordertypes");

		List<OrderType> orderTypeList = new ArrayList<OrderType>();

		Session session = null;

		try {

			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Querying Order Type Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			countQuery = session.createQuery("SELECT COUNT(id) FROM OrderType");
			Long rowsTotal = (Long) countQuery.uniqueResult();
			if (rowsTotal == 0) {
				request.response("Order Type is Empty : ", HttpResponseStatus.BAD_REQUEST).end();
				return future;
			}

			LOGGER.info("4) Querying OrderType List !!!!!!!!!!!!!");
			Query<OrderType> listQuery = null;

				listQuery = session.createQuery("FROM OrderType", OrderType.class);

			List<OrderType> result = listQuery.getResultList();
			Iterator<OrderType> it = result.iterator();

			while (it.hasNext()) {
				OrderType aOrderType = it.next();
				orderTypeList.add(aOrderType);
			}

			LOGGER.info("5) Return Order Type List !!!!!!!!!!!!!!!!!!!");
			future.complete(orderTypeList);
		} catch (Exception e) {
			LOGGER.error("Db Operation Failed !!!!!!!!!!!!!", e);
		} finally {
			LOGGER.info("6) Close Session !!!!!!!!!!!!!!!!!");
			if (session != null) {
				session.close();
			}
		}
		return future;
	}



}
