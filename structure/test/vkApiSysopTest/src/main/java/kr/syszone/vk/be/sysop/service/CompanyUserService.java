package kr.syszone.vk.be.sysop.service;

import java.util.ArrayList;
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
import kr.syszone.vk.be.db.entity.CompanyUser;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.CompanyUserApis;
import kr.syszone.vk.be.sysop.model.CompanyUserVo;
import kr.syszone.vk.be.sysop.model.ListHelper;

@Path("/api/v1/sysop")
public class CompanyUserService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompanyUserService.class);

	private CompanyUserApis companyApis;

	@JsonRequest
	@Operation(summary = "COMPANY USER API - Endpoint Information")
	@GET
	@Path("/companyuser")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyUserApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on Company USER!!");
		if (companyApis == null) {
			companyApis = new CompanyUserApis();
		}
		CompletableFuture<CompanyUserApis> future = new CompletableFuture<>();
		future.complete(companyApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "COMPANY USER API - List")
	@GET
	@Path("/companyuser/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<CompanyUserVo>> getCompanyList(@Parameter @QueryParam("search") String search,
			@Parameter @QueryParam("order") String order, @Parameter @QueryParam("orderBy") String orderBy,
			@Parameter @QueryParam("page") Integer page, @Parameter @QueryParam("limit") Integer limit,
			@Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<CompanyUserVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/sysop/companyuser/list");

		ListHelper<CompanyUserVo> listHelper = new ListHelper<CompanyUserVo>();
		List<CompanyUserVo> companyUserList = new ArrayList<CompanyUserVo>();
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

			LOGGER.info("3) Querying Company USER Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery("SELECT COUNT(cu.id)" + " FROM CompanyUser AS cu"
						+ " LEFT JOIN Company c ON (cu.companyId = c.id)"
						+ " LEFT JOIN CompanyGroup cg ON (cu.groupId = cg.id)"
						+ " LEFT JOIN User u ON (cu.userId = u.id)"
						+ " WHERE cg.name LIKE :keyword OR cu.userId LIKE :keyword OR cu.memo LIKE :keyword OR cu.role LIKE :keyword OR c.companyName LIKE :keyword OR cg.name LIKE :keyword OR u.name LIKE :keyword");

				countQuery.setParameter("keyword", '%' + search + '%');
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) FROM CompanyUser");
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();
			LOGGER.error("rowsTotal : {} ", rowsTotal);
			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying Company USER List !!!!!!!!!!!!!");
			Query<?> listQuery = null;
			if (search != null && search.length() > 0) {
				listQuery = session
						.createSQLQuery("SELECT cu.*, c.company_name as cName, cg.name AS cgName, u.name AS uName"
								+ " FROM company_user AS cu" + " LEFT JOIN company c ON cu.company_id = c.id"
								+ " LEFT JOIN company_group cg ON cu.group_id = cg.id"
								+ " LEFT JOIN user u ON u.id = cu.user_id"
								+ " WHERE cg.name LIKE :keyword OR cu.user_id LIKE :keyword OR cu.memo LIKE :keyword OR cu.role LIKE :keyword OR c.company_name LIKE :keyword OR cg.name LIKE :keyword OR u.name LIKE :keyword")
						.addEntity("company_user", CompanyUser.class).addScalar("cName", new StringType())
						.addScalar("cgName", new StringType()).addScalar("uName", new StringType());
				listQuery.setParameter("keyword", '%' + search + '%');

			} else {
				listQuery = session
						.createSQLQuery("SELECT cu.*, c.company_name as cName, cg.name AS cgName, u.name AS uName"
								+ " FROM company_user AS cu" + " LEFT JOIN company c ON cu.company_id = c.id"
								+ " LEFT JOIN company_group cg ON cu.group_id = cg.id"
								+ " LEFT JOIN user u ON u.id = cu.user_id")
						.addEntity("company_user", CompanyUser.class).addScalar("cName", new StringType())
						.addScalar("cgName", new StringType()).addScalar("uName", new StringType());
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			@SuppressWarnings("unchecked")
			List<Object[]> result = (List<Object[]>) listQuery.list();
			result.stream().forEach((record) -> {
				CompanyUser aCompanyUser = (CompanyUser) record[0];
				String cName = (String) record[1];
				String cgName = (String) record[2];
				String uName = (String) record[3];
				companyUserList.add(new CompanyUserVo(aCompanyUser, cName, cgName, uName));
			});
			LOGGER.info("5) Return ComapnyInfo !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
			listHelper.setResult(companyUserList);
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
	@Operation(summary = "Company USER API - Create")
	@POST
	@Path("/companyuser")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyUser> companySignUp(@Parameter(hidden = true) RakamHttpRequest request,
			@RequestBody CompanyUser body) {

		LOGGER.info("POST : /api/v1/sysop/companyuser/");
		CompletableFuture<CompanyUser> future = new CompletableFuture<>();

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
	@Operation(summary = "Company USER API - Update")
	@PUT
	@Path("/companyuser/{cuId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyUserVo> companyUpdate(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("cuId") Long cuId, @Parameter @RequestBody CompanyUser cu) {

		LOGGER.info("UPDATE : /api/v1/sysop/companyuser/");

		CompletableFuture<CompanyUserVo> future = new CompletableFuture<>();
		if (cuId == null || cu == null || cuId != cu.getId()) {
			request.response("Invalid Comapny User : " + cuId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Update Company USER Entity !!!!!!!!!!!!!");
			CompanyUser updateCu = new CompanyUser();
			updateCu.setId(cu.getId());
			updateCu.setCompanyId(cu.getCompanyId());
			updateCu.setGroupId(cu.getGroupId());
			updateCu.setUserId(cu.getUserId());
			updateCu.setRole(cu.getRole());
			updateCu.setMemo(cu.getMemo());
			session.update(updateCu);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return CompanyUserVo !!!!!!!!!!!!!!!!!!!");
			future.complete(new CompanyUserVo(cu));
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
	@Operation(summary = "Company USER API - Delete")
	@DELETE
	@Path("/companyuser/{cuId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyUserVo> companyDelete(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("cuId") Long cuId) {

		CompletableFuture<CompanyUserVo> future = new CompletableFuture<>();

		if (cuId == null || cuId == 0) {
			request.response("Invalid CompanyUser : " + cuId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		LOGGER.info("DELETE : /api/v1/sysop/companyuser/");

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
			CompanyUser aCu = session.find(CompanyUser.class, cuId);
			session.delete(aCu);
			
			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return Company !!!!!!!!!!!!!!!!!!!");
			future.complete(new CompanyUserVo(aCu));
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
