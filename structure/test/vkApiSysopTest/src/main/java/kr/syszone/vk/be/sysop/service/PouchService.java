package kr.syszone.vk.be.sysop.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
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
import kr.syszone.vk.be.db.entity.Company;
import kr.syszone.vk.be.db.entity.Pouch;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.ListHelper;
import kr.syszone.vk.be.sysop.model.PouchApis;
import kr.syszone.vk.be.sysop.model.PouchVo;

@Path("/api/v1/sysop")
public class PouchService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PouchService.class);

	private PouchApis pouchApis;

	@JsonRequest
	@Operation(summary = "Pouch API - Endpoint Information")
	@GET
	@Path("/pouch")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<PouchApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on Pouch!!");
		if (pouchApis == null) {
			pouchApis = new PouchApis();
		}
		CompletableFuture<PouchApis> future = new CompletableFuture<>();
		future.complete(pouchApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "COMPANY API - List")
	@GET
	@Path("/pouch/companylist")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<PouchVo>> getCompanyList(@Parameter(hidden = true) RakamHttpRequest request) {

		CompletableFuture<List<PouchVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/sysop/pouch/companylist");

		List<PouchVo> companyList = new ArrayList<PouchVo>();

		Session session = null;

		try {

			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Querying Company Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			countQuery = session.createQuery("SELECT COUNT(id) FROM Company");
			Long rowsTotal = (Long) countQuery.uniqueResult();
			if (rowsTotal == 0) {
				request.response("Company is Empty : ", HttpResponseStatus.BAD_REQUEST).end();
				return future;
			}

			LOGGER.info("4) Querying Company List !!!!!!!!!!!!!");
			Query<Company> listQuery = null;
			listQuery = session.createQuery("FROM Company", Company.class);
			List<Company> result = listQuery.getResultList();

			Iterator<Company> it = result.iterator();
			while (it.hasNext()) {
				Company aCompany = it.next();
				PouchVo vo = new PouchVo(aCompany.getCompanyName(), aCompany.getId());
				companyList.add(vo);
			}

			LOGGER.info("5) Return ComapnyInfo !!!!!!!!!!!!!!!!!!!");
			future.complete(companyList);
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
	@Operation(summary = "Pouch API - List")
	@GET
	@Path("/pouch/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<PouchVo>> getPouchList(@Parameter @QueryParam("search") String search,
			@Parameter @QueryParam("order") String order, @Parameter @QueryParam("orderBy") String orderBy,
			@Parameter @QueryParam("page") Integer page, @Parameter @QueryParam("limit") Integer limit,
			@Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<PouchVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/sysop/pouch/list");

		ListHelper<PouchVo> listHelper = new ListHelper<PouchVo>();
		List<PouchVo> PouchList = new ArrayList<PouchVo>();
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

			LOGGER.info("3) Querying Pouch Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session
						.createQuery("SELECT COUNT(p.id) FROM Pouch AS p LEFT JOIN Company c ON (p.companyId = c.id) "
								+ "WHERE p.name LIKE :keyword OR p.memo LIKE :keyword OR c.companyName LIKE :keyword");
				countQuery.setParameter("keyword", '%' + search + '%');
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) FROM Pouch");
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();
			LOGGER.error("rowsTotal : {} ", rowsTotal);

			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying Pouch List !!!!!!!!!!!!!");
			NativeQuery<?> listQuery = null;
			if (search != null && search.length() > 0) {
				listQuery = session
						.createSQLQuery("SELECT p.*, c.*  FROM Pouch AS p LEFT JOIN Company c ON (p.company_id = c.id) "
								+ "WHERE p.name LIKE :keyword OR p.memo LIKE :keyword OR c.company_name LIKE :keyword")
						.addEntity("Pouch", Pouch.class).addEntity("Company", Company.class);
				listQuery.setParameter("keyword", '%' + search + '%');
			} else {
				listQuery = session.createSQLQuery(
						"SELECT p.*, c.id as cId, c.company_name as cName FROM Pouch AS p LEFT JOIN Company c ON (p.company_id = c.id) ")
						.addEntity("Pouch", Pouch.class).addScalar("cId", new StringType())
						.addScalar("cName", new StringType());
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			@SuppressWarnings("unchecked")
			List<Object[]> result = (List<Object[]>) listQuery.list();
			result.stream().forEach((record) -> {
				Pouch aPouch = (Pouch) record[0];
				String cId = (String) record[1];
				String cName = (String) record[2];
				PouchList.add(new PouchVo(aPouch, cName, cId));
			});

			LOGGER.info("5) Return PouchVO !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
			listHelper.setResult(PouchList);
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
	@Operation(summary = "Pouch API - Create")
	@POST
	@Path("/pouch")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<Pouch> PouchCreate(@Parameter(hidden = true) RakamHttpRequest request,
			@RequestBody Pouch body) {

		LOGGER.info("POST : /api/v1/sysop/pouch/");
		CompletableFuture<Pouch> future = new CompletableFuture<>();

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
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			body.setTsRegister(timestamp);
			session.save(body);

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
	@Operation(summary = "Pouch API - Update")
	@PUT
	@Path("/pouch/{pouchId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<PouchVo> PouchUpdate(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("pouchId") String pouchId, @Parameter @RequestBody PouchVo pouchVo) {

		CompletableFuture<PouchVo> future = new CompletableFuture<>();
		if (pouchId == null || pouchVo == null || (Long.parseLong(pouchId) != pouchVo.getId())) {
			request.response("Invalid PouchId : " + pouchId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Save Pouch Entity !!!!!!!!!!!!!");
			Pouch pouch = pouchVo.getPouch();
			session.update(pouch);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return PouchVo !!!!!!!!!!!!!!!!!!!");
			future.complete(new PouchVo(pouch));
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
	@Operation(summary = "Pouch API - Delete")
	@DELETE
	@Path("/pouch/{pouchid}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<PouchVo> PouchDelete(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("pouchid") Long pouchId) {

		CompletableFuture<PouchVo> future = new CompletableFuture<>();

		if (pouchId == null || pouchId == 0) {
			request.response("Invalid PouchId : " + pouchId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		LOGGER.info("DELETE : /api/v1/sysop/pouch");

		Session session = null;
		Transaction transaction = null;
		try {
			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Begin Transaction !!!!!!!!!!!!!");
			transaction = session.beginTransaction();

			LOGGER.info("4) Delete Pouch !!!!!!!!!!!!!!!!!!!");
			Pouch aPouch = session.find(Pouch.class, pouchId);
			session.delete(aPouch);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return Pouch !!!!!!!!!!!!!!!!!!!");
			future.complete(new PouchVo(aPouch));
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
