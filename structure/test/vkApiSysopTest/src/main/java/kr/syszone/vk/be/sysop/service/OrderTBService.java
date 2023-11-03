package kr.syszone.vk.be.sysop.service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import kr.syszone.vk.be.db.HibernateUtil;
import kr.syszone.vk.be.db.InfluxDbUtil;
import kr.syszone.vk.be.db.entity.Order;
import kr.syszone.vk.be.db.entity.OrderDelivery;
import kr.syszone.vk.be.db.entity.OrderDetail;
import kr.syszone.vk.be.db.entity.Product;
import kr.syszone.vk.be.db.entity.ProductHistory;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.GnssDataVo;
import kr.syszone.vk.be.sysop.model.ListHelper;
import kr.syszone.vk.be.sysop.model.OrderApis;
import kr.syszone.vk.be.sysop.model.OrderTBDetailVo;
import kr.syszone.vk.be.sysop.model.OrderTBVo;
import kr.syszone.vk.be.sysop.model.SensorDataVo;

@Path("/api/v1/sysop")
public class OrderTBService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderTBService.class);

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
	@Operation(summary = "Order Transfer Business API - List")
	@GET
	@Path("/ordertb/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<OrderTBVo>> getorderList(@Parameter @QueryParam("search") String search,
			@Parameter @QueryParam("order") String order, @Parameter @QueryParam("orderBy") String orderBy,
			@Parameter @QueryParam("page") Integer page, @Parameter @QueryParam("limit") Integer limit,
			@Parameter @QueryParam("tab") String tab, @Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<OrderTBVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/ordertb/list");

		ListHelper<OrderTBVo> listHelper = new ListHelper<OrderTBVo>();
		List<OrderTBVo> orderList = new ArrayList<OrderTBVo>();
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
//			if (search.equals("m2cloud")) {
//				search = "31";
//			}
			if (search != null && search.length() > 0) {
				if (tab.equals("29")) {// vsk sensorID
					countQuery = session.createSQLQuery("SELECT COUNT(DISTINCT(o.id)) FROM `order` AS o"
							+ " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
							+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
							+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
							+ " LEFT JOIN order_detail od ON (od.order_id = o.id)"
							+ " LEFT JOIN sensor s ON (od.sensor_id = s.id)" + " WHERE o.order_type = 0"
							+ " AND s.si_id = 29"
							+ " AND (o.name LIKE :keyword OR o.memo LIKE :keyword OR ec.company_name LIKE :keyword OR oc.company_name LIKE :keyword)"
							+ " ORDER BY o.ts_start DESC").setParameter("keyword", "%" + search + "%");
				} else if (tab.equals("optilo_delivery")) {
					countQuery = session.createSQLQuery("SELECT COUNT(DISTINCT(o.id)) FROM `order` AS o"
							+ " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
							+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
							+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
							+ " LEFT JOIN order_detail od ON (od.order_id = o.id)"
							+ " LEFT JOIN sensor s ON (od.sensor_id = s.id)" + " WHERE o.order_type = 0"
							+ " AND s.si_id = '31' AND o.name LIKE '%_v'"
							+ " AND (o.name LIKE :keyword OR o.memo LIKE :keyword OR ec.company_name LIKE :keyword OR oc.company_name LIKE :keyword)"
							+ " ORDER BY o.ts_start DESC")
//							.setParameter("search", "%" + search + "%")
							.setParameter("keyword", "%" + search + "%");
				} else if (tab.equals("optilo_return")) {
					countQuery = session.createSQLQuery("SELECT COUNT(DISTINCT(o.id)) FROM `order` AS o"
							+ " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
							+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
							+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
							+ " LEFT JOIN order_detail od ON (od.order_id = o.id)"
							+ " LEFT JOIN sensor s ON (od.sensor_id = s.id)" + " WHERE o.order_type = 0"
							+ " AND s.si_id = '31' AND o.name LIKE '%_p'"
							+ " AND (o.name LIKE :keyword OR o.memo LIKE :keyword OR ec.company_name LIKE :keyword OR oc.company_name LIKE :keyword)"
							+ " ORDER BY o.ts_start DESC")
//							.setParameter("search", "%" + search + "%_p")
							.setParameter("keyword", "%" + search + "%");
				}
				BigInteger rowsTotal = (BigInteger) countQuery.uniqueResult();
				listHelper.setTotalCount(rowsTotal.longValue());
			} else {
				if (tab.equals("29")) {// vsk sensorID
					countQuery = session.createSQLQuery("SELECT COUNT(DISTINCT(o.id)) FROM `order` AS o"
							+ " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
							+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
							+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
							+ " LEFT JOIN order_detail od ON (od.order_id = o.id)"
							+ " LEFT JOIN sensor s ON (od.sensor_id = s.id)" + " WHERE o.order_type = 0"
							+ " AND s.si_id = 29");
				} else if (tab.equals("optilo_delivery")) {
					countQuery = session.createSQLQuery("SELECT COUNT(DISTINCT(o.id)) FROM `order` AS o"
							+ " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
							+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
							+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
							+ " LEFT JOIN order_detail od ON (od.order_id = o.id)"
							+ " LEFT JOIN sensor s ON (od.sensor_id = s.id)" + " WHERE o.order_type = 0"
							+ " AND s.si_id = '31'" + " AND o.name LIKE '%_v'");
				} else if (tab.equals("optilo_return")) {
					countQuery = session.createSQLQuery("SELECT COUNT(DISTINCT(o.id)) FROM `order` AS o"
							+ " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
							+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
							+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
							+ " LEFT JOIN order_detail od ON (od.order_id = o.id)"
							+ " LEFT JOIN sensor s ON (od.sensor_id = s.id)" + " WHERE o.order_type = 0"
							+ " AND s.si_id = '31'" + " AND o.name LIKE '%_p'");
				}
//				countQuery = session.createQuery("SELECT COUNT(id) FROM Order WHERE orderType = 0");
				BigInteger rowsTotal = (BigInteger) countQuery.uniqueResult();
				listHelper.setTotalCount(rowsTotal.longValue());
			}
			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();
			LOGGER.info("4) Querying Order List !!!!!!!!!!!!!");
			Query<?> listQuery = null;
			if (search != null && search.length() > 0) {
				if (tab.equals("29")) {
					listQuery = session.createSQLQuery("SELECT DISTINCT o.*," + " ot.name as otName,"
							+ " oc.company_name as ocName," + " ec.company_name as ecName," + " od.*"
							+ " FROM `order` AS o" + " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
							+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
							+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
							+ " LEFT JOIN order_delivery od ON (o.id = od.order_id)"
							+ " LEFT JOIN order_detail odt ON (odt.order_id = o.id)"
							+ " LEFT JOIN sensor s ON (odt.sensor_id = s.id)" + " WHERE o.order_type = 0"
							+ " AND s.si_id = 29"
							+ " AND (o.name LIKE :keyword OR o.memo LIKE :keyword OR ec.company_name LIKE :keyword OR oc.company_name LIKE :keyword)"
							+ " ORDER BY o.ts_start DESC").setParameter("keyword", "%" + search + "%")
							.addEntity("o", Order.class).addScalar("otName", new StringType())
							.addScalar("ocName", new StringType()).addScalar("ecName", new StringType())
							.addEntity("od", OrderDelivery.class);
				} else if (tab.equals("optilo_delivery")) {
					listQuery = session.createSQLQuery("SELECT DISTINCT o.*," + " ot.name as otName,"
							+ " oc.company_name as ocName," + " ec.company_name as ecName," + " od.*"
							+ " FROM `order` AS o" + " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
							+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
							+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
							+ " LEFT JOIN order_delivery od ON (o.id = od.order_id)"
							+ " LEFT JOIN order_detail odt ON (odt.order_id = o.id)"
							+ " LEFT JOIN sensor s ON (odt.sensor_id = s.id)" + " WHERE o.order_type = 0"
							+ " AND s.si_id = '31' AND o.name LIKE '%_v'"
							+ " AND (o.name LIKE :keyword OR o.memo LIKE :keyword OR ec.company_name LIKE :keyword OR oc.company_name LIKE :keyword)"
							+ " ORDER BY o.ts_start DESC")
//							.setParameter("search", "%" + search + "%")
							.setParameter("keyword", "%" + search + "%").addEntity("o", Order.class)
							.addScalar("otName", new StringType()).addScalar("ocName", new StringType())
							.addScalar("ecName", new StringType()).addEntity("od", OrderDelivery.class);
				} else if (tab.equals("optilo_return")) {
					listQuery = session.createSQLQuery("SELECT DISTINCT o.*," + " ot.name as otName,"
							+ " oc.company_name as ocName," + " ec.company_name as ecName," + " od.*"
							+ " FROM `order` AS o" + " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
							+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
							+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
							+ " LEFT JOIN order_delivery od ON (o.id = od.order_id)"
							+ " LEFT JOIN order_detail odt ON (odt.order_id = o.id)"
							+ " LEFT JOIN sensor s ON (odt.sensor_id = s.id)" + " WHERE o.order_type = 0"
							+ " AND s.si_id = '31' AND o.name LIKE '%_p'"
							+ " AND (o.name LIKE :keyword OR o.memo LIKE :keyword OR ec.company_name LIKE :keyword OR oc.company_name LIKE :keyword)"
							+ " ORDER BY o.ts_start DESC")
//							.setParameter("search", "%" + search + "%_p")
							.setParameter("keyword", "%" + search + "%").addEntity("o", Order.class)
							.addScalar("otName", new StringType()).addScalar("ocName", new StringType())
							.addScalar("ecName", new StringType()).addEntity("od", OrderDelivery.class);
				}
			} else {

				if (tab.equals("29")) {
					listQuery = session
							.createSQLQuery("SELECT DISTINCT o.*," + " ot.name as otName,"
									+ " oc.company_name as ocName," + " ec.company_name as ecName," + " od.*"
									+ " FROM `order` AS o" + " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
									+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
									+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
									+ " LEFT JOIN order_delivery od ON (o.id = od.order_id)"
									+ " LEFT JOIN order_detail odt ON (odt.order_id = o.id)"
									+ " LEFT JOIN sensor s ON (odt.sensor_id = s.id)" + " WHERE o.order_type = 0"
									+ " AND s.si_id = '29'" + " ORDER BY o.ts_start DESC")
							.addEntity("o", Order.class).addScalar("otName", new StringType())
							.addScalar("ocName", new StringType()).addScalar("ecName", new StringType())
							.addEntity("od", OrderDelivery.class);
				} else if (tab.equals("optilo_delivery")) {
					listQuery = session
							.createSQLQuery("SELECT DISTINCT o.*," + " ot.name as otName,"
									+ " oc.company_name as ocName," + " ec.company_name as ecName," + " od.*"
									+ " FROM `order` AS o" + " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
									+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
									+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
									+ " LEFT JOIN order_delivery od ON (o.id = od.order_id)"
									+ " LEFT JOIN order_detail odt ON (odt.order_id = o.id)"
									+ " LEFT JOIN sensor s ON (odt.sensor_id = s.id)" + " WHERE o.order_type = 0"
									+ " AND s.si_id = '31'" + " AND o.name LIKE '%_v'" + " ORDER BY o.ts_start DESC")
							.addEntity("o", Order.class).addScalar("otName", new StringType())
							.addScalar("ocName", new StringType()).addScalar("ecName", new StringType())
							.addEntity("od", OrderDelivery.class);
				} else if (tab.equals("optilo_return")) {
					listQuery = session
							.createSQLQuery("SELECT DISTINCT o.*," + " ot.name as otName,"
									+ " oc.company_name as ocName," + " ec.company_name as ecName," + " od.*"
									+ " FROM `order` AS o" + " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
									+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
									+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
									+ " LEFT JOIN order_delivery od ON (o.id = od.order_id)"
									+ " LEFT JOIN order_detail odt ON (odt.order_id = o.id)"
									+ " LEFT JOIN sensor s ON (odt.sensor_id = s.id)" + " WHERE o.order_type = 0"
									+ " AND s.si_id = '31'" + " AND o.name LIKE '%_p'" + " ORDER BY o.ts_start DESC")
							.addEntity("o", Order.class).addScalar("otName", new StringType())
							.addScalar("ocName", new StringType()).addScalar("ecName", new StringType())
							.addEntity("od", OrderDelivery.class);
				}

//				listQuery = session
//						.createSQLQuery("SELECT " + "   o.*," + "   ot.name as otName,"
//								+ "   oc.company_name as ocName," + "   ec.company_name as ecName," + "   od.*"
//								+ " FROM `order` AS o" + " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
//								+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
//								+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
//								+ " LEFT JOIN order_delivery od ON (o.id = od.order_id)"
//								+ " WHERE o.order_type = 0 ORDER BY o.ts_start DESC")
//						.addEntity("o", Order.class).addScalar("otName", new StringType())
//						.addScalar("ocName", new StringType()).addScalar("ecName", new StringType())
//						.addEntity("od", OrderDelivery.class);
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
				OrderDelivery aOd = (OrderDelivery) record[4];
//				Long pId = (Long) record[5];
//				String pName = (String) record[6];
//				Double pTL = (Double) record[7];
//				Double pTH = (Double) record[8];
				orderList.add(new OrderTBVo(aOrder, otName, ocName, ecName, aOd));
			});

//			for (OrderTBVo vo : orderList) {
//				@SuppressWarnings("unchecked")
//				List<Object[]> sensorList = (List<Object[]>) session
//						.createSQLQuery("SELECT od.*, si.data_type as sType FROM order_detail od"
//								+ " LEFT JOIN sensor s ON (od.sensor_id = s.id)"
//								+ " LEFT JOIN sensor_info si ON (si.id = s.si_id)" + " WHERE order_id = :orderId")
//						.addEntity("od", OrderDetail.class).addScalar("sType", new IntegerType())
//						.setParameter("orderId", vo.getId()).list();
//				List<String> gnssLogList = new ArrayList<String>();
//				List<String> sensorLogList = new ArrayList<String>();
//				sensorList.stream().forEach((record) -> {
//					OrderDetail aOd = (OrderDetail) record[0];
//					if (aOd.getSensorId() != null) {
//						int sType = (Integer) record[1];
//						if (sType == 13) {// Sensor
//							sensorLogList.add(aOd.getSensorId());
//							vo.setSensorId(sensorLogList);
//						}
//						if (sType == 28) {// Gnss
//							gnssLogList.add(aOd.getSensorId());
//							vo.setGnssId(gnssLogList);
//						}
//					}
//
//				});
//
//			}

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
	@Path("/ordertb/setvehicle")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<OrderTBVo> setVehicle(@Parameter(hidden = true) RakamHttpRequest request,
			@RequestBody OrderTBVo body) {

		LOGGER.info("POST : /orderdetail/tb/setvehicle");
		CompletableFuture<OrderTBVo> future = new CompletableFuture<>();

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
			Query<?> getOrderDetail = null;
			getOrderDetail = session.createQuery("FROM OrderDetail" + " WHERE orderId = :keyword");
			getOrderDetail.setParameter("keyword", body.getId());

			OrderDetail od = (OrderDetail) getOrderDetail.getSingleResult();
			od.setVehicleId(body.getVehicleId());
			session.update(od);

			Order order = session.find(Order.class, od.getOrderId());
			order.setState(101);
			order.setTsStart(new Timestamp(System.currentTimeMillis()));
			session.update(order);

			ProductHistory ph = new ProductHistory();
			ph.setProductId(od.getProductId());
			ph.setWorkType(2);
			ph.setName("배차완료");
			ph.setPrevCompanyId(order.getOrderCompanyId());
			ph.setNextCompanyId(order.getExecuteCompanyId());
			ph.setTsWorkStart(new Timestamp(System.currentTimeMillis()));
			session.save(ph);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

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
	@Operation(summary = "Order API - Create")
	@POST
	@Path("/ordertb/deliveryend")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<OrderTBVo> deliveryEnd(@Parameter(hidden = true) RakamHttpRequest request,
			@RequestBody OrderTBVo body) {

		LOGGER.info("POST : /orderdetail/tb/deliveryend");
		CompletableFuture<OrderTBVo> future = new CompletableFuture<>();

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

			Order order = session.find(Order.class, body.getId());
			order.setState(102);
			order.setTsEnd(new Timestamp(System.currentTimeMillis()));
			session.update(order);

			Query<?> getOrderDetail = null;
			getOrderDetail = session.createQuery("FROM OrderDetail" + " WHERE orderId = :keyword");
			getOrderDetail.setParameter("keyword", order.getId());

			OrderDetail od = (OrderDetail) getOrderDetail.getSingleResult();

			ProductHistory ph = new ProductHistory();
			ph.setProductId(od.getProductId());
			ph.setWorkType(2);
			ph.setName("배송완료");
			ph.setPrevCompanyId(order.getOrderCompanyId());
			ph.setNextCompanyId(order.getExecuteCompanyId());
			ph.setTsWorkStart(new Timestamp(System.currentTimeMillis()));
			session.save(ph);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

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
	@Operation(summary = "Sensor API - Sensor List")
	@GET
	@Path("/service/tb/companies/{companyId}/orders/{orderId}/sensors")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<OrderTBDetailVo>> getSensorList(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("companyId") String companyId, @Parameter @PathParam("orderId") Long orderId) {

		LOGGER.info("GET : /service/tb/companies/" + companyId + "/orders/" + orderId + "/sensors");
		CompletableFuture<List<OrderTBDetailVo>> future = new CompletableFuture<>();

		if (companyId == null || companyId.length() == 0) {
			request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (orderId == null || orderId < 1) {
			request.response("Invalid orderId : " + orderId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Querying SAVE !!!!!!!!!!!!!!!");

			Query<?> getOrderDetail = null;
			@SuppressWarnings("unchecked")
			List<OrderDetail> odList = session.createQuery("FROM OrderDetail" + " WHERE orderId = :keyword")
					.setParameter("keyword", orderId).list();
			LOGGER.info(odList.toString());

			Map<Long, OrderTBDetailVo> result = new HashMap<>();

			for (OrderDetail od : odList) {
				Product p = (Product) session.find(Product.class, od.getProductId());

				@SuppressWarnings("unchecked")
				List<Object[]> sensorList = (List<Object[]>) session
						.createSQLQuery("SELECT od.*, si.data_type as sType FROM order_detail od"
								+ " LEFT JOIN sensor s ON (od.sensor_id = s.id)"
								+ " LEFT JOIN sensor_info si ON (si.id = s.si_id)"
								+ " WHERE order_id = :orderId AND sensor_id = :sensorId")
						.addEntity("od", OrderDetail.class).addScalar("sType", new IntegerType())
						.setParameter("orderId", orderId).setParameter("sensorId", od.getSensorId()).list();
				sensorList.stream().forEach((record) -> {

					OrderTBDetailVo vo = result.get(p.getId());
					if (vo == null) {
						vo = new OrderTBDetailVo(p);
						result.put(p.getId(), vo);
					}

					OrderDetail aOd = (OrderDetail) record[0];
					if (aOd.getSensorId() != null) {
						int sType = (Integer) record[1];
						if (sType == 13 | sType == 3) {// Sensor
							vo.setSensorId(aOd.getSensorId());
						}
						if (sType == 4124 | sType == 3) {// Gnss
							vo.setGnssId(aOd.getSensorId());
						}
					}
				});
			}

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			future.complete(new ArrayList<OrderTBDetailVo>(result.values()));
		} catch (Exception e) {
			LOGGER.error("Db Operation Failed !!!!!!!!!!!!!", e);
			request.response(e.getLocalizedMessage(), HttpResponseStatus.BAD_REQUEST).end();
		}
		return future;

	}

	@JsonRequest
	@Operation(summary = "Sensor API - Sensor Data")
	@GET
	@Path("/service/tb/companies/{companyId}/sensors/{sensorId}/data")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<SensorDataVo>> getSensorData(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("companyId") String companyId, @Parameter @PathParam("sensorId") String sensorId,
			@Parameter @QueryParam("sd") String sd, @Parameter(required = false) @QueryParam("ed") String ed) {

		CompletableFuture<List<SensorDataVo>> future = new CompletableFuture<>();

		if (companyId == null || companyId.length() == 0) {
			request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (sensorId == null || companyId.length() == 0) {
			request.response("Invalid SensorId : " + sensorId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (sd == null || sd.length() < 10) {
			request.response("Invalid startDate : " + sd, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (ed == null || ed.length() < 10) {
			request.response("Invalid endDate : " + ed, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		try {
			InfluxDBClient dbClient = InfluxDbUtil.getClient();
			QueryApi qa = dbClient.getQueryApi();

			sd = sd.replaceFirst("\\s", "T");
			ed = ed.replaceFirst("\\s", "T");

			String ssd = sd + "+09:00";
			String sed = ed + "+09:00";

			LOGGER.info(ssd + " / " + sed);
			StringBuilder sb = new StringBuilder();
			sb.append("from(bucket: \"valuekeeper\")").append('\n');
			sb.append("  |> range(start: ").append(ssd).append(',');
			sb.append("           stop: ").append(sed).append(')').append('\n');
			sb.append("  |> filter(fn: (r) => r[\"_measurement\"] == \"sensorLog\" and ");
			sb.append("                       r[\"sid\"] == \"").append(sensorId).append("\")\n");
			sb.append("  |> pivot(rowKey: [\"_time\", \"sid\"], columnKey: [\"_field\"], valueColumn: \"_value\")\n");
			sb.append("  |> sort(columns: [\"_time\"])\n");

			LOGGER.info("InfluxQL : \n{}", sb.toString());

			List<SensorDataVo> slList = new ArrayList<SensorDataVo>();
			List<FluxTable> tables = qa.query(sb.toString());
			for (FluxTable fluxTable : tables) {
				List<FluxRecord> records = fluxTable.getRecords();
				for (FluxRecord fluxRecord : records) {
					SensorDataVo sl = new SensorDataVo();

					sl.setSid((String) fluxRecord.getValueByKey("sid"));
					sl.setTime(
							new Timestamp(Instant.parse(fluxRecord.getValueByKey("_time").toString()).toEpochMilli()));
					sl.setTemperature((Double) fluxRecord.getValueByKey("temperature"));
					slList.add(sl);
				}
			}

			future.complete(slList);
		} catch (Exception e) {
			LOGGER.error("Db Operation Failed !!!!!!!!!!!!!", e);
			request.response(e.getLocalizedMessage(), HttpResponseStatus.BAD_REQUEST).end();
		}
		return future;

	}

	@JsonRequest
	@Operation(summary = "Gnss API - GPS Data")
	@GET
	@Path("/service/tb/companies/{companyId}/gnss/{sensorId}/data")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<GnssDataVo>> getGnssData(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("companyId") String companyId, @Parameter @PathParam("sensorId") String sensorId,
			@Parameter @QueryParam("sd") String sd, @Parameter(required = false) @QueryParam("ed") String ed) {

		CompletableFuture<List<GnssDataVo>> future = new CompletableFuture<>();

		if (companyId == null || companyId.length() == 0) {
			request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (sensorId == null || companyId.length() == 0) {
			request.response("Invalid SensorId : " + sensorId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (sd == null || sd.length() < 10) {
			request.response("Invalid startDate : " + sd, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (ed == null || ed.length() < 10) {
			request.response("Invalid endDate : " + ed, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		try {
			InfluxDBClient dbClient = InfluxDbUtil.getClient();
			QueryApi qa = dbClient.getQueryApi();

			sd = sd.replaceFirst("\\s", "T");
			ed = ed.replaceFirst("\\s", "T");

			String ssd = sd + "+09:00";
			String sed = ed + "+09:00";

			LOGGER.info(ssd + " / " + sed);
			StringBuilder sb = new StringBuilder();
			sb.append("from(bucket: \"valuekeeper\")").append('\n');
			sb.append("  |> range(start: ").append(ssd).append(',');
			sb.append("           stop: ").append(sed).append(')').append('\n');
			sb.append("  |> filter(fn: (r) => r[\"_measurement\"] == \"gnssLog\" and ");
			sb.append("                       r[\"sid\"] == \"").append(sensorId).append("\")\n");
			sb.append("  |> pivot(rowKey: [\"_time\", \"sid\"], columnKey: [\"_field\"], valueColumn: \"_value\")\n");
			sb.append("  |> sort(columns: [\"_time\"])\n");

			LOGGER.info("InfluxQL : \n{}", sb.toString());

			List<GnssDataVo> slList = new ArrayList<GnssDataVo>();
			List<FluxTable> tables = qa.query(sb.toString());
			for (FluxTable fluxTable : tables) {
				List<FluxRecord> records = fluxTable.getRecords();
				for (FluxRecord fluxRecord : records) {
					GnssDataVo gl = new GnssDataVo();

					gl.setSid((String) fluxRecord.getValueByKey("sid"));
					gl.setTime(
							new Timestamp(Instant.parse(fluxRecord.getValueByKey("_time").toString()).toEpochMilli()));
//					gl.setStoreTime(new Timestamp(
//							Instant.parse(fluxRecord.getValueByKey("storeTime").toString()).toEpochMilli()));
					gl.setLongitude((Double) fluxRecord.getValueByKey("longitude"));
					gl.setLatitude((Double) fluxRecord.getValueByKey("latitude"));
					gl.setAccuracy((Double) fluxRecord.getValueByKey("accuracy"));
					gl.setAngle((Double) fluxRecord.getValueByKey("angle"));
					gl.setSpeed((Double) fluxRecord.getValueByKey("speed"));

					slList.add(gl);
				}
			}

			future.complete(slList);
		} catch (Exception e) {
			LOGGER.error("Db Operation Failed !!!!!!!!!!!!!", e);
			request.response(e.getLocalizedMessage(), HttpResponseStatus.BAD_REQUEST).end();
		}
		return future;

	}

}
