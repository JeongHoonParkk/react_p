package kr.syszone.vk.be.sysop.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
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
import kr.syszone.vk.be.db.entity.OrderDetail;
import kr.syszone.vk.be.db.entity.Product;
import kr.syszone.vk.be.db.entity.ProductHistory;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.ListHelper;
import kr.syszone.vk.be.sysop.model.ProductApis;
import kr.syszone.vk.be.sysop.model.ProductVo;
import kr.syszone.vk.be.sysop.model.SelectProductVo;

@Path("/api/v1/sysop")
public class ProductService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

	private ProductApis productApis;

	@JsonRequest
	@Operation(summary = "PRODUCT API - Endpoint Information")
	@GET
	@Path("/product")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ProductApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on PRODUCT!!");
		if (productApis == null) {
			productApis = new ProductApis();
		}
		CompletableFuture<ProductApis> future = new CompletableFuture<>();
		future.complete(productApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "PRODUCT API - List")
	@GET
	@Path("/product/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<ProductVo>> getProductList(@Parameter @QueryParam("search") String search,
			@Parameter @QueryParam("order") String order, @Parameter @QueryParam("orderBy") String orderBy,
			@Parameter @QueryParam("page") Integer page, @Parameter @QueryParam("limit") Integer limit,
			@Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<ProductVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/product/list");

		ListHelper<ProductVo> listHelper = new ListHelper<ProductVo>();
		List<ProductVo> productList = new ArrayList<ProductVo>();
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

			LOGGER.info("3) Querying PRODUCT Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery("SELECT COUNT(p.id)" + " FROM Product AS p"
						+ " LEFT JOIN Company c ON (p.ownerId = c.id OR p.managerId = c.id)"
						+ " WHERE p.name LIKE :keyword OR p.memo LIKE :keyword OR c.name LIKE :keyword OR p.state LIKE :keyword");
				countQuery.setParameter("keyword", '%' + search + '%');
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) FROM Product");
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();
			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying PRODUCT List !!!!!!!!!!!!!");
			Query<?> listQuery = null;
			if (search != null && search.length() > 0) {
				listQuery = session.createSQLQuery(
						"SELECT p.*, oc.company_name as ocName, mc.company_name as mcName, ref.name as refName FROM product AS p"
								+ " LEFT JOIN company oc ON (p.owner_id = oc.id)"
								+ " LEFT JOIN company mc ON (p.manager_id = mc.id)"
								+ " LEFT JOIN product ref ON (p.ref_id = ref.id)"
								+ " WHERE p.name LIKE :keyword OR p.memo LIKE :keyword OR c.name LIKE :keyword OR p.state LIKE :keyword OR ref.name LIKE :keyword")
						.addEntity("company_vehicle", Product.class).addScalar("ocName", new StringType())
						.addScalar("mcName", new StringType());
				listQuery.setParameter("keyword", '%' + search + '%');

			} else {
				listQuery = session.createSQLQuery(
						"SELECT p.*, oc.company_name as ocName, mc.company_name as mcName, ref.name as refName FROM product AS p"
								+ " LEFT JOIN company oc ON (p.owner_id = oc.id)"
								+ " LEFT JOIN company mc ON (p.manager_id = mc.id)"
								+ " LEFT JOIN product ref ON (p.ref_id = ref.id)")
						.addEntity("company_vehicle", Product.class).addScalar("ocName", new StringType())
						.addScalar("mcName", new StringType()).addScalar("refName", new StringType());
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			@SuppressWarnings("unchecked")
			List<Object[]> result = (List<Object[]>) listQuery.list();
			result.stream().forEach((record) -> {
				Product aProduct = (Product) record[0];
				String ocName = (String) record[1];
				String mcName = (String) record[2];
				String refName = (String) record[3];
				productList.add(new ProductVo(aProduct, ocName, mcName, refName));
			});

			LOGGER.info("5) Return ComapnyInfo !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
			listHelper.setResult(productList);
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
	@Operation(summary = "PRODUCT API - Create")
	@POST
	@Path("/product")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<Product> productCreate(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @RequestBody Product body) {

		LOGGER.info("CREATE : /api/v1/product/");
		CompletableFuture<Product> future = new CompletableFuture<>();

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
			body.setTsRegister(new Timestamp(System.currentTimeMillis()));
			body.setTorSec(0);
			session.save(body);

			ProductHistory ph = new ProductHistory();
			ph.setProductId(body.getId());
			ph.setWorkType(0);
			ph.setName("최초등록");
			ph.setPrevCompanyId(body.getOwnerId());
			ph.setNextCompanyId(body.getOwnerId());
			ph.setTsWorkStart(new Timestamp(System.currentTimeMillis()));
			ph.setTsWorkEnd(new Timestamp(System.currentTimeMillis()));
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
	@Operation(summary = "PRODUCT API - Update")
	@PUT
	@Path("/product/{pId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ProductVo> productUpdate(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("pId") Long pId, @Parameter @RequestBody Product p) {

		LOGGER.info("UPDATE : /api/v1/sysop/product/");

		CompletableFuture<ProductVo> future = new CompletableFuture<>();
		if (pId == null || p == null || pId != p.getId()) {
			request.response("Invalid Pruduct : " + pId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Update PRODUCT Entity !!!!!!!!!!!!!");
			Product updateP = session.find(Product.class, pId);
			updateP.setOwnerId(p.getOwnerId());
			updateP.setManagerId(p.getManagerId());
			updateP.setAmount(p.getAmount());
			updateP.setState(p.getState());
			updateP.setTsModify(new Timestamp(System.currentTimeMillis()));
			updateP.setTempHigh(p.getTempHigh());
			updateP.setTempLow(p.getTempLow());
			updateP.setName(p.getName());
			updateP.setMemo(p.getMemo());
			updateP.setRefId(p.getRefId());

			session.update(updateP);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return CompanyUserVo !!!!!!!!!!!!!!!!!!!");
			future.complete(new ProductVo(p));
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
	@Operation(summary = "PRODUCT API - Delete")
	@DELETE
	@Path("/product/{pId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ProductVo> productDelete(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("pId") Long pId) {

		CompletableFuture<ProductVo> future = new CompletableFuture<>();

		if (pId == null || pId == 0) {
			request.response("Invalid PRODUCT : " + pId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		LOGGER.info("DELETE : /api/v1/product/");

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

			Query<?> result = null;
			result = session.createQuery("FROM OrderDetail" + " WHERE productId = :keyword");
			result.setParameter("keyword", pId);
			
			@SuppressWarnings("unchecked")
			List<OrderDetail> odList = (List<OrderDetail>) result.list();
			for (OrderDetail orderDetail : odList) {
				session.createQuery("DELETE Order" + " WHERE id = :keyword").setParameter("keyword", orderDetail.getOrderId())
				.executeUpdate();
				session.createQuery("DELETE OrderDelivery" + " WHERE orderId = :keyword").setParameter("keyword", orderDetail.getOrderId())
				.executeUpdate();
			}

			session.createQuery("DELETE OrderDetail" + " WHERE productId = :keyword").setParameter("keyword", pId)
			.executeUpdate();

			session.createQuery("DELETE ProductHistory" + " WHERE productId = :keyword").setParameter("keyword", pId)
					.executeUpdate();

			Product aCv = session.find(Product.class, pId);
			session.delete(aCv);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return Company !!!!!!!!!!!!!!!!!!!");
			future.complete(new ProductVo(aCv));
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
	@Operation(summary = "Product API - Product Search List")
	@GET
	@Path("/products/select")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<SelectProductVo>> getSelectproductList(
			@Parameter(hidden = true) RakamHttpRequest request, @Parameter @QueryParam("pn") String productNm) {

		CompletableFuture<List<SelectProductVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/products");

		List<SelectProductVo> productList = new ArrayList<SelectProductVo>();

		Session session = null;

		try {

			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Querying Product Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			countQuery = session.createQuery("SELECT COUNT(id) FROM Product");
			Long rowsTotal = (Long) countQuery.uniqueResult();
			if (rowsTotal == 0) {
				request.response("Product is Empty : ", HttpResponseStatus.BAD_REQUEST).end();
				return future;
			}

			LOGGER.info("4) Querying Product List !!!!!!!!!!!!!");
			Query<Product> listQuery = null;

			if (productNm != null) {
				if (productNm.length() > 0) {
					listQuery = session.createQuery("FROM Product WHERE name LIKE :keyword", Product.class)
							.setParameter("keyword", "%" + productNm + "%");
				}
			} else {
				listQuery = session.createQuery("FROM Product", Product.class);
			}

			List<Product> result = listQuery.getResultList();
			Iterator<Product> it = result.iterator();

			while (it.hasNext()) {
				SelectProductVo vo = new SelectProductVo();
				Product aProduct = it.next();
				vo.setProductId(aProduct.getId());
				vo.setProductName(aProduct.getName());
				productList.add(vo);
			}

			LOGGER.info("5) Return Select Product List !!!!!!!!!!!!!!!!!!!");
			future.complete(productList);
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
	@Operation(summary = "Product API - Product Search List")
	@GET
	@Path("/products")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<SelectProductVo>> getAllProductList(
			@Parameter(hidden = true) RakamHttpRequest request) {

		CompletableFuture<List<SelectProductVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/products");

		List<SelectProductVo> productList = new ArrayList<SelectProductVo>();

		Session session = null;

		try {

			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Querying Product Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			countQuery = session.createQuery("SELECT COUNT(id) FROM Product");
			Long rowsTotal = (Long) countQuery.uniqueResult();
			if (rowsTotal == 0) {
				request.response("Product is Empty : ", HttpResponseStatus.BAD_REQUEST).end();
				return future;
			}

			LOGGER.info("4) Querying Product List !!!!!!!!!!!!!");
			Query<Product> listQuery = null;

			listQuery = session.createQuery("FROM Product", Product.class);

			List<Product> result = listQuery.getResultList();
			Iterator<Product> it = result.iterator();

			while (it.hasNext()) {
				SelectProductVo vo = new SelectProductVo();
				Product aProduct = it.next();
				vo.setProductId(aProduct.getId());
				vo.setProductName(aProduct.getName());
				productList.add(vo);
			}

			LOGGER.info("5) Return All Product List !!!!!!!!!!!!!!!!!!!");
			future.complete(productList);
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
