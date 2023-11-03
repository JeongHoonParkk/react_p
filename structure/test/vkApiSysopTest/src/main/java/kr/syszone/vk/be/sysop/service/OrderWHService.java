package kr.syszone.vk.be.sysop.service;

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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import kr.syszone.vk.be.db.HibernateUtil;
import kr.syszone.vk.be.db.entity.Order;
import kr.syszone.vk.be.db.entity.OrderDetail;
import kr.syszone.vk.be.db.entity.ProductHistory;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.ListHelper;
import kr.syszone.vk.be.sysop.model.OrderApis;
import kr.syszone.vk.be.sysop.model.OrderTBVo;
import kr.syszone.vk.be.sysop.model.OrderVo;
import kr.syszone.vk.be.sysop.model.OrderWHVo;

@Path("/api/v1/sysop")
public class OrderWHService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderWHService.class);

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
	@Operation(summary = "Order Warehouse API - List")
	@GET
	@Path("/orderwh/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<OrderVo>> getorderList(@Parameter @QueryParam("search") String search,
			@Parameter @QueryParam("order") String order, @Parameter @QueryParam("orderBy") String orderBy,
			@Parameter @QueryParam("page") Integer page, @Parameter @QueryParam("limit") Integer limit,
			@Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<OrderVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/orderwh/list");

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
				countQuery = session.createQuery("SELECT COUNT(o.id)" + " FROM Order AS o"
						+ " LEFT JOIN Company c ON (o.orderCompanyId = c.id OR o.executeCompanyId = c.id)"
						+ " WHERE o.order_type = 1 AND (o.name LIKE :keyword OR o.memo LIKE :keyword OR c.name LIKE :keyword)");
				countQuery.setParameter("keyword", '%' + search + '%');
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) FROM Order");
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();
			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying Order List !!!!!!!!!!!!!");
			Query<?> listQuery = null;
			if (search != null && search.length() > 0) {
				listQuery = session.createSQLQuery(
						"SELECT o.*, oc.company_name as ocName, ec.company_name as ecName FROM `order` AS o"
								+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
								+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
								+ " WHERE o.order_type = 1 AND (o.name LIKE :keyword OR o.memo LIKE :keyword OR oc.name LIKE :keyword OR ec.name LIKE :keyword)")
						.addEntity("o", Order.class).addScalar("ocName", new StringType())
						.addScalar("ecName", new StringType());
				listQuery.setParameter("keyword", '%' + search + '%');

			} else {
				listQuery = session.createSQLQuery(
						"SELECT o.*, ot.name as otName, oc.company_name as ocName, ec.company_name as ecName, p.id as opId, p.name as opName"
								+ " FROM `order` AS o" + " LEFT JOIN order_type ot ON (o.order_type = ot.id)"
								+ " LEFT JOIN company oc ON (o.order_company_id = oc.id)"
								+ " LEFT JOIN company ec ON (o.execute_company_id = ec.id)"
								+ " LEFT JOIN order_detail odt ON (o.id = odt.order_id)"
								+ " LEFT JOIN product p ON (odt.product_id = p.id)" + " WHERE o.order_type = 1")
						.addEntity("o", Order.class).addScalar("otName", new StringType())
						.addScalar("ocName", new StringType()).addScalar("ecName", new StringType())
						.addScalar("opId", new LongType()).addScalar("opName", new StringType());
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
				Long pId = (Long) record[4];
				String pName = (String) record[5];
				orderList.add(new OrderVo(aOrder, otName, ocName, ecName, pId, pName));
			});
			LOGGER.info("5) Return ComapnyInfo !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
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
	@Operation(summary = "OrderWH in WareHouse API - Endpoint Information")
	@POST
	@Path("/orderwh/in")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<OrderWHVo> inWH(@Parameter(hidden = true) RakamHttpRequest request,
			@RequestBody Order body) {
		LOGGER.info("POST : /orderwh/in");
		CompletableFuture<OrderWHVo> future = new CompletableFuture<>();

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
		    Order od = session.find(Order.class, body.getId());
		    od.setState(201);
		    od.setTsStart(new Timestamp(System.currentTimeMillis()));
		    od.setTsEnd(new Timestamp(System.currentTimeMillis()));
		    session.update(od);
		    
		    Query<?> getOrderDetail = null;
			getOrderDetail = session
					.createQuery("FROM OrderDetail" + " WHERE orderId = :keyword");
			getOrderDetail.setParameter("keyword", body.getId());

			OrderDetail oDetail = (OrderDetail) getOrderDetail.getSingleResult();
			
			ProductHistory ph = new ProductHistory();
			ph.setProductId(oDetail.getProductId());
			ph.setWorkType(2);
			ph.setName("입고");
			ph.setPrevCompanyId(od.getOrderCompanyId());
			ph.setNextCompanyId(od.getExecuteCompanyId());
			ph.setTsWorkStart(new Timestamp(System.currentTimeMillis()));
			session.save(ph);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			future.complete(new OrderWHVo(od));
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
	@Operation(summary = "OrderWH in WareHouse API - Endpoint Information")
	@POST
	@Path("/orderwh/out")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<OrderWHVo> outWH(@Parameter(hidden = true) RakamHttpRequest request,
			@RequestBody Order body) {
		LOGGER.info("POST : /orderwh/in");
		CompletableFuture<OrderWHVo> future = new CompletableFuture<>();

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
		    Order od = session.find(Order.class, body.getId());
		    od.setState(202);
		    od.setTsStart(new Timestamp(System.currentTimeMillis()));
		    od.setTsEnd(new Timestamp(System.currentTimeMillis()));
		    session.update(od);
		    
		    Query<?> getOrderDetail = null;
			getOrderDetail = session
					.createQuery("FROM OrderDetail" + " WHERE orderId = :keyword");
			getOrderDetail.setParameter("keyword", body.getId());

			OrderDetail oDetail = (OrderDetail) getOrderDetail.getSingleResult();
			
			ProductHistory ph = new ProductHistory();
			ph.setProductId(oDetail.getProductId());
			ph.setWorkType(2);
			ph.setName("출고");
			ph.setPrevCompanyId(od.getOrderCompanyId());
			ph.setNextCompanyId(od.getExecuteCompanyId());
			ph.setTsWorkStart(new Timestamp(System.currentTimeMillis()));
			session.save(ph);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			future.complete(new OrderWHVo(od));
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
