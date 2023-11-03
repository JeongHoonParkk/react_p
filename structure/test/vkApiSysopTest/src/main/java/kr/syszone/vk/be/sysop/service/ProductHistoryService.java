package kr.syszone.vk.be.sysop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import kr.syszone.vk.be.db.HibernateUtil;
import kr.syszone.vk.be.db.entity.Product;
import kr.syszone.vk.be.db.entity.ProductHistory;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.ListHelper;
import kr.syszone.vk.be.sysop.model.ProductHistoryApis;
import kr.syszone.vk.be.sysop.model.ProductHistoryVo;

@Path("/api/v1/sysop")
public class ProductHistoryService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductHistoryService.class);

	private ProductHistoryApis productHistoryApis;

	@JsonRequest
	@Operation(summary = "Product History API - Endpoint Information")
	@GET
	@Path("/producthistory")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ProductHistoryApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on PRODUCT!!");
		if (productHistoryApis == null) {
			productHistoryApis = new ProductHistoryApis();
		}
		CompletableFuture<ProductHistoryApis> future = new CompletableFuture<>();
		future.complete(productHistoryApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "Product History API - List")
	@GET
	@Path("/producthistory/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<ProductHistoryVo>> getPHistoryList(
			@Parameter @QueryParam("search") String search, @Parameter @QueryParam("order") String order,
			@Parameter @QueryParam("orderBy") String orderBy, @Parameter @QueryParam("page") Integer page,
			@Parameter @QueryParam("limit") Integer limit, @Parameter @QueryParam("pId") Long pId,
			@Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<ProductHistoryVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/producthistory/list");

		ListHelper<ProductHistoryVo> listHelper = new ListHelper<ProductHistoryVo>();
		List<ProductHistoryVo> phList = new ArrayList<ProductHistoryVo>();
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

			LOGGER.info("3) Querying Product History Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery("SELECT COUNT(ph.id)" + " FROM ProductHistory AS ph"
						+ " LEFT JOIN Product p ON (ph.productId = p.id)"
						+ " LEFT JOIN Company c ON (ph.prevCompanyId = c.id OR ph.nextCompanyId = c.id)"
						+ " WHERE p.name LIKE :keyword OR ph.name LIKE :keyword OR ph.memo LIKE :keyword OR c.name LIKE :keyword");
				countQuery.setParameter("keyword", '%' + search + '%');
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) FROM ProductHistory");
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();
			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying ProductHistory List !!!!!!!!!!!!!");
			Query<?> listQuery = null;
			if (search != null && search.length() > 0) {
				listQuery = session.createSQLQuery(
						"SELECT p.*, oc.company_name as ocName, mc.company_name as mcName, ref.name as refName FROM product AS p"
								+ " LEFT JOIN company oc ON (p.owner_id = oc.id)"
								+ " LEFT JOIN company mc ON (p.manager_id = mc.id)"
								+ " LEFT JOIN product ref ON (p.ref_id = ref.id)"
								+ " WHERE p.name LIKE :keyword OR p.memo LIKE :keyword OR c.name LIKE :keyword OR p.state LIKE :keyword OR ref.name LIKE :keyword")
						.addEntity("company_vehicle", ProductHistory.class).addScalar("ocName", new StringType())
						.addScalar("mcName", new StringType());
				listQuery.setParameter("keyword", '%' + search + '%');

			} else {
				listQuery = session.createSQLQuery(
						"SELECT ph.*, preC.company_name as preCName, nextC.company_name as nextCName, p.name as pName FROM product_history AS ph"
								+ " LEFT JOIN company preC ON (ph.prev_company_id = preC.id)"
								+ " LEFT JOIN company nextC ON (ph.next_company_id = nextC.id)"
								+ " LEFT JOIN product p ON (ph.product_id = p.id)"
								+ " WHERE product_id = :pId")
						.addEntity("ph", ProductHistory.class).addScalar("preCName", new StringType())
						.addScalar("nextCName", new StringType()).addScalar("pName", new StringType()).setParameter("pId", pId);
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			@SuppressWarnings("unchecked")
			List<Object[]> result = (List<Object[]>) listQuery.list();
			result.stream().forEach((record) -> {
				ProductHistory aPh = (ProductHistory) record[0];
				String preCName = (String) record[1];
				String nextCName = (String) record[2];
				String pName = (String) record[3];
				phList.add(new ProductHistoryVo(aPh, preCName, nextCName, pName));
			});
			LOGGER.info("5) Return ComapnyInfo !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
			listHelper.setResult(phList);
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

}
