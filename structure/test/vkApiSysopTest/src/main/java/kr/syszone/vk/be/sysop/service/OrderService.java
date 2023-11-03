package kr.syszone.vk.be.sysop.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import kr.syszone.vk.be.db.HibernateUtil;
import kr.syszone.vk.be.db.entity.Order;
import kr.syszone.vk.be.db.entity.OrderDelivery;
import kr.syszone.vk.be.db.entity.OrderDetail;
import kr.syszone.vk.be.db.entity.Product;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.ListHelper;
import kr.syszone.vk.be.sysop.model.OrderApis;
import kr.syszone.vk.be.sysop.model.OrderVo;

@Path("/api/v1/sysop")
public class OrderService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

	private OrderApis orderApis;

	@JsonRequest
	@Operation(summary = "Order API - Endpoint Information")
	@GET
	@Path("/order")
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

	@JsonRequest
	@Operation(summary = "Order API - List")
	@GET
	@Path("/order/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<OrderVo>> getOrderList(@Parameter @QueryParam("search") String search,
			@Parameter @QueryParam("order") String order, @Parameter @QueryParam("orderBy") String orderBy,
			@Parameter @QueryParam("page") Integer page, @Parameter @QueryParam("limit") Integer limit,
			@Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<OrderVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/order/list");

		ListHelper<OrderVo> listHelper = new ListHelper<OrderVo>();
		List<OrderVo> orderList = new ArrayList<OrderVo>();
		Session session = null;
		try {
			listHelper.setSearch(search);
			listHelper.setOrder(order);
			listHelper.setOrderBy(orderBy);
			listHelper.setRequestPage(page == null ? 1 : page);
			listHelper.setRowsPerPage(limit == null ? 10 : limit);

			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Querying Order Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createSQLQuery(
						"SELECT COUNT(o.id) FROM `order` AS o"
								+ " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
								+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
								+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
								+ " LEFT JOIN (SELECT order_id, product_id FROM order_detail LIMIT 0) odt ON (o.id = odt.order_id)"
						+ " WHERE o.name LIKE :keyword OR o.memo LIKE :keyword OR oc.company_name LIKE :keyword OR ec.company_name LIKE :keyword");
				countQuery.setParameter("keyword", '%' + search + '%');
				BigInteger rowsTotal = (BigInteger) countQuery.uniqueResult();
				listHelper.setTotalCount(rowsTotal.longValue());
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) FROM Order");
				Long rowsTotal = (Long) countQuery.uniqueResult();
				listHelper.setTotalCount(rowsTotal);
			}
			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying Order List !!!!!!!!!!!!!");
			Query<?> listQuery = null;
			if (search != null && search.length() > 0) {
				listQuery = session.createSQLQuery(
						"SELECT o.*, ot.name as otName, oc.company_name as ocName, ec.company_name as ecName, od.*, p.id as pId, p.name as pName FROM `order` AS o"
								+ " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
								+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
								+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
								+ " LEFT JOIN order_delivery od ON (o.id = od.order_id)"
								+ " LEFT JOIN (SELECT order_id, product_id FROM order_detail LIMIT 0) odt ON (o.id = odt.order_id)"
								+ " LEFT JOIN product p ON (odt.product_id = p.id)"
								+ " WHERE o.name LIKE :keyword OR o.memo LIKE :keyword OR oc.company_name LIKE :keyword OR ec.company_name LIKE :keyword")
						.addEntity("o", Order.class).addScalar("otName", new StringType())
						.addScalar("ocName", new StringType()).addScalar("ecName", new StringType())
						.addEntity("od", OrderDelivery.class).addScalar("pId", new LongType())
						.addScalar("pName", new StringType());
				listQuery.setParameter("keyword", '%' + search + '%');

			} else {
				listQuery = session.createSQLQuery(
						"SELECT o.*, ot.name as otName, oc.company_name as ocName, ec.company_name as ecName, od.*, p.id as pId, p.name as pName FROM `order` AS o"
								+ " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
								+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
								+ " LEFT JOIN order_delivery od ON (o.id = od.order_id)"
								+ " LEFT JOIN (SELECT order_id, product_id FROM order_detail LIMIT 0) odt ON (o.id = odt.order_id)"
								+ " LEFT JOIN product p ON (odt.product_id = p.id)"
								+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)")
						.addEntity("o", Order.class).addScalar("otName", new StringType())
						.addScalar("ocName", new StringType()).addScalar("ecName", new StringType())
						.addEntity("od", OrderDelivery.class).addScalar("pId", new LongType())
						.addScalar("pName", new StringType());
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			@SuppressWarnings("unchecked")
			List<Object[]> result = (List<Object[]>) listQuery.list();
			result.stream().forEach((record) -> {
				Order aOrder = (Order) record[0];
				String otName = (String) record[1];
				String ocName = (String) record[2];
				String ecName = (String) record[3];
				OrderDelivery od = (OrderDelivery) record[4];
				Long pId = (Long) record[5];
				String pName = (String) record[6];
				orderList.add(new OrderVo(aOrder, otName, ocName, ecName, od, pId, pName));
			});
			LOGGER.info("5) Return ComapnyInfo !!!!!!!!!!!!!!!!!!!");

			listHelper.setResult(orderList);
			future.complete(listHelper);
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

	@JsonRequest
	@Operation(summary = "Order API - Create")
	@POST
	@Path("/order")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<Order> orderSave(@Parameter(hidden = true) RakamHttpRequest request,
			@RequestBody OrderVo body) {

		LOGGER.info("CREATE : /api/v1/order/");
		CompletableFuture<Order> future = new CompletableFuture<>();

		Session session = null;
		Transaction transaction = null;

		try {
			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();
			LOGGER.info("3) Begin Transaction !!!!!!!!!!!!!");
			transaction = session.beginTransaction();

			LOGGER.info("4) Querying SAVE !!!!!!!!!!!!!!!");
			Order saveO = new Order();
			saveO.setName(body.getName());
			saveO.setOrderType(body.getOrderType());
			saveO.setOrderCompanyId(body.getOrderCompanyId());
			saveO.setExecuteCompanyId(body.getExecuteCompanyId());
			saveO.setMemo(body.getMemo());
			saveO.setState(0);
			saveO.setTsRegister(new Timestamp(System.currentTimeMillis()));

			session.save(saveO);

			OrderDetail od = new OrderDetail();
			od.setOrderId(saveO.getId());
			od.setProductId(body.getProductId());

			session.save(od);

			if (saveO.getOrderType() == 0) { // OrderType 운송
				OrderDelivery oDelivery = new OrderDelivery();
				oDelivery.setOrderId(saveO.getId());
				oDelivery.setFromName(body.getFromName());
				oDelivery.setFromPostcode(body.getFromPostcode());
				oDelivery.setFromAddressPost(body.getFromAddressPost());
				oDelivery.setFromAddressDetail(body.getFromAddressDetail());
				oDelivery.setFromLatitude(body.getFromLatitude());
				oDelivery.setFromLongitude(body.getFromLongitude());
				oDelivery.setToName(body.getToName());
				oDelivery.setToPostcode(body.getToPostcode());
				oDelivery.setToAddressPost(body.getToAddressPost());
				oDelivery.setToAddressDetail(body.getToAddressDetail());
				oDelivery.setToLatitude(body.getToLatitude());
				oDelivery.setToLongitude(body.getToLongitude());

				session.save(oDelivery);
			}

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			future.complete(saveO);
		} catch (Exception e) {
			LOGGER.error("6) Operation Failed !!!!!!!!!!!!!", e);
			if (transaction != null) {
				transaction.rollback();
			}
			request.response(e.getMessage(), HttpResponseStatus.BAD_REQUEST).end();
		} finally {
			LOGGER.info("6) Close Session !!!!!!!!!!!!!!!!!");
			if (session != null) {
				session.close();
			}
		}

		return future;
	}

	@JsonRequest
	@Operation(summary = "Order API - Update")
	@PUT
	@Path("/order/{oId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<OrderVo> productUpdate(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("oId") Long oId, @Parameter @RequestBody OrderVo body) {

		LOGGER.info("UPDATE : /api/v1/sysop/order/");

		CompletableFuture<OrderVo> future = new CompletableFuture<>();
		if (oId == null || body == null || oId != body.getId()) {
			request.response("Invalid Pruduct : " + oId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		Session session = null;
		Transaction transaction = null;
		try {
			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Begin Transaction !!!!!!!!!!!!!");
			transaction = session.beginTransaction();

			LOGGER.info("4) Update Order Info !!!!!!!!!!!!!");
			Order uOrder = session.find(Order.class, oId);
			uOrder.setName(body.getName());
			uOrder.setOrderType(body.getOrderType());
			uOrder.setOrderCompanyId(body.getOrderCompanyId());
			uOrder.setExecuteCompanyId(body.getExecuteCompanyId());
			uOrder.setMemo(body.getMemo());
			if (uOrder.getOrderType() == 0) {
				Query<?> getOrderDelivery = null;
				getOrderDelivery = session.createQuery("FROM OrderDelivery" + " WHERE orderId = :keyword");
				getOrderDelivery.setParameter("keyword", oId);
				OrderDelivery uOD = (OrderDelivery) getOrderDelivery.getSingleResult();
				uOD.setFromName(body.getFromName());
				uOD.setFromPostcode(body.getFromPostcode());
				uOD.setFromAddressPost(body.getFromAddressPost());
				uOD.setFromLatitude(body.getFromLatitude());
				uOD.setFromLongitude(body.getFromLongitude());
				uOD.setToName(body.getToName());
				uOD.setToPostcode(body.getToPostcode());
				uOD.setToAddressPost(body.getToAddressPost());
				uOD.setToLatitude(body.getToLatitude());
				uOD.setToLongitude(body.getToLongitude());
				session.update(uOD);
			}
			session.update(uOrder);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return OrderVo !!!!!!!!!!!!!!!!!!!");
			future.complete(body);
		} catch (Exception e) {
			LOGGER.error("6) Operation Failed !!!!!!!!!!!!!", e);
			if (transaction != null) {
				transaction.rollback();
			}
			request.response(e.getMessage(), HttpResponseStatus.BAD_REQUEST).end();
		} finally {
			LOGGER.info("6) Close Session !!!!!!!!!!!!!!!!!");
			if (session != null) {
				session.close();
			}
		}

		return future;
	}

	@JsonRequest
	@Operation(summary = "Order API - Delete")
	@DELETE
	@Path("/order/{pId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<OrderVo> productDelete(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("pId") Long pId) {

		CompletableFuture<OrderVo> future = new CompletableFuture<>();

		if (pId == null || pId == 0) {
			request.response("Invalid PRODUCT : " + pId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		LOGGER.info("DELETE : /api/v1/order/");

		Session session = null;
		Transaction transaction = null;
		try {
			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Begin Transaction !!!!!!!!!!!!!");
			transaction = session.beginTransaction();

			LOGGER.info("4) Delete Company !!!!!!!!!!!!!!!!!!!");
			Product aCv = session.find(Product.class, pId);
			session.delete(aCv);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return Company !!!!!!!!!!!!!!!!!!!");
//			future.complete(new OrderVo(aCv));
		} catch (Exception e) {
			LOGGER.error("6) Operation Failed !!!!!!!!!!!!!", e);
			if (transaction != null) {
				transaction.rollback();
			}
			request.response(e.getMessage(), HttpResponseStatus.BAD_REQUEST).end();
		} finally {
			LOGGER.info("6) Close Session !!!!!!!!!!!!!!!!!");
			if (session != null) {
				session.close();
			}
		}

		return future;
	}

}
