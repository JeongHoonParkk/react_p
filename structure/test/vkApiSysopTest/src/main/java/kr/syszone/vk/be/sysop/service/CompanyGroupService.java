package kr.syszone.vk.be.sysop.service;

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
import kr.syszone.vk.be.db.entity.CompanyGroup;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.CompanyGroupApis;
import kr.syszone.vk.be.sysop.model.CompanyGroupVo;
import kr.syszone.vk.be.sysop.model.ListHelper;
import kr.syszone.vk.be.sysop.model.SelectCompanyGroupVo;

@Path("/api/v1/sysop")
public class CompanyGroupService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompanyGroupService.class);

	private CompanyGroupApis companyGroupApis;

	@JsonRequest
	@Operation(summary = "CompanyGroup API - Endpoint Information")
	@GET
	@Path("/companygroup")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyGroupApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on CompanyGroup!!");
		if (companyGroupApis == null) {
			companyGroupApis = new CompanyGroupApis();
		}
		CompletableFuture<CompanyGroupApis> future = new CompletableFuture<>();
		future.complete(companyGroupApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "CompanyGroup API - List")
	@GET
	@Path("/companygroup/companylist")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<CompanyGroupVo>> getCompanyList(@Parameter(hidden = true) RakamHttpRequest request) {

		CompletableFuture<List<CompanyGroupVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/companygroup/companylist");

		List<CompanyGroupVo> companyList = new ArrayList<CompanyGroupVo>();

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
				CompanyGroupVo vo = new CompanyGroupVo(aCompany.getCompanyName(), aCompany.getId());
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
	@Operation(summary = "CompanyGroup API - List")
	@GET
	@Path("/companygroup/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<CompanyGroupVo>> getCompanyGroupList(
			@Parameter @QueryParam("search") String search, @Parameter @QueryParam("order") String order,
			@Parameter @QueryParam("orderBy") String orderBy, @Parameter @QueryParam("page") Integer page,
			@Parameter @QueryParam("limit") Integer limit, @Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<CompanyGroupVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/companygroup/list");

		ListHelper<CompanyGroupVo> listHelper = new ListHelper<CompanyGroupVo>();
		List<CompanyGroupVo> CompanyGroupList = new ArrayList<CompanyGroupVo>();
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

			LOGGER.info("3) Querying CompanyGroup Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery(
						"SELECT COUNT(p.id) FROM CompanyGroup AS p LEFT JOIN Company c ON (p.companyId = c.id) "
								+ "WHERE p.name LIKE :keyword OR p.memo LIKE :keyword OR c.companyName LIKE :keyword");
				countQuery.setParameter("keyword", '%' + search + '%');
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) FROM CompanyGroup");
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();
			LOGGER.error("rowsTotal : {} ", rowsTotal);

			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying CompanyGroup List !!!!!!!!!!!!!");
			NativeQuery<?> listQuery = null;
			if (search != null && search.length() > 0) {
				listQuery = session.createSQLQuery(
						"SELECT cg.*, c.id as cId, c.company_name as cName FROM company_group AS cg LEFT JOIN company c ON (cg.company_id = c.id) "
								+ "WHERE cg.name LIKE :keyword OR cg.memo LIKE :keyword OR cg.role LIKE : keyword OR c.company_name LIKE :keyword")
						.addEntity("CompanyGroup", CompanyGroup.class).addScalar("cId", new StringType())
						.addScalar("cName", new StringType());
				listQuery.setParameter("keyword", '%' + search + '%');
			} else {
				listQuery = session.createSQLQuery(
						"SELECT cg.*, c.id as cId, c.company_name as cName FROM company_group AS cg LEFT JOIN company c ON cg.company_id = c.id ")
						.addEntity("company_group", CompanyGroup.class).addScalar("cId", new StringType())
						.addScalar("cName", new StringType());
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			@SuppressWarnings("unchecked")
			List<Object[]> result = (List<Object[]>) listQuery.list();
			result.stream().forEach((record) -> {
				CompanyGroup aCompanyGroup = (CompanyGroup) record[0];
				String cId = (String) record[1];
				String cName = (String) record[2];
				CompanyGroupList.add(new CompanyGroupVo(aCompanyGroup, cName, cId));
			});

			LOGGER.info("5) Return CompanyGroupVO !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
			listHelper.setResult(CompanyGroupList);
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
	@Operation(summary = "CompanyGroup API - Create")
	@POST
	@Path("/companygroup")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyGroup> CompanyGroupCreate(@Parameter(hidden = true) RakamHttpRequest request,
			@RequestBody CompanyGroup body) {

		LOGGER.info("POST : /api/v1/companygroup/");
		CompletableFuture<CompanyGroup> future = new CompletableFuture<>();

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
	@Operation(summary = "CompanyGroup API - Update")
	@PUT
	@Path("/companygroup/{cgId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyGroupVo> CompanyGroupUpdate(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("cgId") String companyGroupId,
			@Parameter @RequestBody CompanyGroupVo companyGroupVo) {

		CompletableFuture<CompanyGroupVo> future = new CompletableFuture<>();
		if (companyGroupVo == null || companyGroupVo == null
				|| (Long.parseLong(companyGroupId) != companyGroupVo.getId())) {
			request.response("Invalid CompanyGroupId : " + companyGroupId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Save CompanyGroup Entity !!!!!!!!!!!!!");
			CompanyGroup companyGroup = companyGroupVo.getCompanyGroup();
			session.update(companyGroup);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return CompanyGroupVo !!!!!!!!!!!!!!!!!!!");
			future.complete(new CompanyGroupVo(companyGroup));
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
	@Operation(summary = "CompanyGroup API - Delete")
	@DELETE
	@Path("/companygroup/{cgId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyGroupVo> CompanyGroupDelete(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("cgId") Long companyGroupId) {

		CompletableFuture<CompanyGroupVo> future = new CompletableFuture<>();

		if (companyGroupId == null || companyGroupId == 0) {
			request.response("Invalid CompanyGroupId : " + companyGroupId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		LOGGER.info("DELETE : /api/v1/CompanyGroup");

		Session session = null;
		Transaction transaction = null;
		try {
			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Begin Transaction !!!!!!!!!!!!!");
			transaction = session.beginTransaction();

			LOGGER.info("4) Delete CompanyGroup !!!!!!!!!!!!!!!!!!!");
			CompanyGroup aCompanyGroup = session.find(CompanyGroup.class, companyGroupId);
			session.delete(aCompanyGroup);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return CompanyGroup !!!!!!!!!!!!!!!!!!!");
			future.complete(new CompanyGroupVo(aCompanyGroup));
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
	@Operation(summary = "Company GROUP API - Company Group Select List")
	@GET
	@Path("/companygroup/{comId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<SelectCompanyGroupVo>> getCGroupList(
			@Parameter(hidden = true) RakamHttpRequest request, @Parameter @PathParam("comId") String comId) {

		LOGGER.info("GET : /api/v1/companygroup/{comId}");
		CompletableFuture<List<SelectCompanyGroupVo>> future = new CompletableFuture<>();
		if (comId == null) {
			request.response("Invalid CompanyId : " + comId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		List<SelectCompanyGroupVo> CompanyGroupList = new ArrayList<SelectCompanyGroupVo>();
		Session session = null;

		try {

			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Querying CompanyGroup Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			countQuery = session.createQuery("SELECT COUNT(id) FROM CompanyGroup");
			Long rowsTotal = (Long) countQuery.uniqueResult();
			if (rowsTotal == 0) {
				request.response("Company_Group is Empty : ", HttpResponseStatus.BAD_REQUEST).end();
				return future;
			}

			LOGGER.info("4) Querying  Company Group Select List !!!!!!!!!!!!!");
			Query<CompanyGroup> listQuery = null;
			listQuery = session.createQuery("FROM CompanyGroup WHERE companyId = :comId", CompanyGroup.class)
					.setParameter("comId", comId);

			List<CompanyGroup> result = listQuery.getResultList();
			Iterator<CompanyGroup> it = result.iterator();

			while (it.hasNext()) {
				SelectCompanyGroupVo vo = new SelectCompanyGroupVo();
				CompanyGroup aCompanyGroup = it.next();
				vo.setGroupId(aCompanyGroup.getId());
				vo.setGroupName(aCompanyGroup.getName());
				CompanyGroupList.add(vo);
			}

			LOGGER.info("5) Return ComapnyGroup LIST !!!!!!!!!!!!!!!!!!!");
			future.complete(CompanyGroupList);
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
