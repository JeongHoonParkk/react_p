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
import kr.syszone.vk.be.db.entity.CompanyVehicle;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.CompanyVehicleApis;
import kr.syszone.vk.be.sysop.model.CompanyVehicleVo;
import kr.syszone.vk.be.sysop.model.ListHelper;

@Path("/api/v1/sysop")
public class CompanyVehicleService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompanyVehicleService.class);

	private CompanyVehicleApis companyVehicleApis;

	@JsonRequest
	@Operation(summary = "COMAPANY VEHICLE API - Endpoint Information")
	@GET
	@Path("/companyvehicle")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyVehicleApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on COMAPANY VEHICLE!!");
		if (companyVehicleApis == null) {
			companyVehicleApis = new CompanyVehicleApis();
		}
		CompletableFuture<CompanyVehicleApis> future = new CompletableFuture<>();
		future.complete(companyVehicleApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "COMAPANY VEHICLE API - List")
	@GET
	@Path("/companyvehicle/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<CompanyVehicleVo>> getCompanyList(
			@Parameter @QueryParam("search") String search, @Parameter @QueryParam("order") String order,
			@Parameter @QueryParam("orderBy") String orderBy, @Parameter @QueryParam("page") Integer page,
			@Parameter @QueryParam("limit") Integer limit, @Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<CompanyVehicleVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/companyvehicle/list");

		ListHelper<CompanyVehicleVo> listHelper = new ListHelper<CompanyVehicleVo>();
		List<CompanyVehicleVo> companyVehicleList = new ArrayList<CompanyVehicleVo>();
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

			LOGGER.info("3) Querying COMAPANY VEHICLE Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery("SELECT COUNT(cv.id)" + " FROM CompanyVehicle AS cv"
						+ " LEFT JOIN Company c ON (cv.companyId = c.id)"
						+ " LEFT JOIN Vehicle v ON (cv.groupId = v.id)" + " LEFT JOIN User u ON (cv.userId = u.id)"
						+ " WHERE cv.name LIKE :keyword OR cv.memo LIKE :keyword OR u.id LIKE :keyword OR u.name LIKE :keyword OR c.companyName LIKE :keyword OR v.carNo LIKE :keyword");
				countQuery.setParameter("keyword", '%' + search + '%');
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) FROM CompanyVehicle");
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();
			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying COMAPANY VEHICLE List !!!!!!!!!!!!!");
			Query<?> listQuery = null;
			if (search != null && search.length() > 0) {
				listQuery = session.createSQLQuery(
						"SELECT cv.*, c.company_name as cName, v.car_no AS vCarNo, u.name AS uName FROM company_vehicle AS cv"
								+ " LEFT JOIN company c ON (cv.company_id = c.id)"
								+ " LEFT JOIN vehicle v ON (cv.vehicle_id = v.id)"
								+ " LEFT JOIN user u ON (cv.user_id = u.id)"
								+ " WHERE cv.name LIKE :keyword OR cv.memo LIKE :keyword OR u.id LIKE :keyword OR u.name LIKE :keyword OR c.company_name LIKE :keyword OR v.car_no LIKE :keyword")
						.addEntity("company_vehicle", CompanyVehicle.class).addScalar("cName", new StringType())
						.addScalar("vCarNo", new StringType()).addScalar("uName", new StringType());
				listQuery.setParameter("keyword", '%' + search + '%');

			} else {
				listQuery = session.createSQLQuery(
						"SELECT cv.*, c.company_name as cName, v.car_no AS vCarNo, u.name AS uName FROM company_vehicle AS cv"
								+ " LEFT JOIN company c ON (cv.company_id = c.id)"
								+ " LEFT JOIN vehicle v ON (cv.vehicle_id = v.id)"
								+ " LEFT JOIN user u ON (cv.user_id = u.id)")
						.addEntity("company_vehicle", CompanyVehicle.class).addScalar("cName", new StringType())
						.addScalar("vCarNo", new StringType()).addScalar("uName", new StringType());
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			@SuppressWarnings("unchecked")
			List<Object[]> result = (List<Object[]>) listQuery.list();
			result.stream().forEach((record) -> {
				CompanyVehicle aCompanyVehicle = (CompanyVehicle) record[0];
				String cName = (String) record[1];
				String vCarNo = (String) record[2];
				String uName = (String) record[3];
				companyVehicleList.add(new CompanyVehicleVo(aCompanyVehicle, cName, vCarNo, uName));
			});
			LOGGER.info("5) Return ComapnyInfo !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
			listHelper.setResult(companyVehicleList);
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
	@Operation(summary = "COMAPANY VEHICLE API - Create")
	@POST
	@Path("/companyvehicle")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyVehicle> companySignUp(@Parameter(hidden = true) RakamHttpRequest request,
			@RequestBody CompanyVehicle body) {

		LOGGER.info("POST : /api/v1/companyvehicle/");
		CompletableFuture<CompanyVehicle> future = new CompletableFuture<>();

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
	@Operation(summary = "COMAPANY VEHICLE API - Update")
	@PUT
	@Path("/companyvehicle/{cvId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyVehicleVo> companyUpdate(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("cvId") Long cvId, @Parameter @RequestBody CompanyVehicle cv) {

		LOGGER.info("UPDATE : /api/v1/sysop/companyvehicle/");

		CompletableFuture<CompanyVehicleVo> future = new CompletableFuture<>();
		if (cvId == null || cv == null || cvId != cv.getId()) {
			request.response("Invalid Comapny Vehicle : " + cvId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Update COMAPANY VEHICLE Entity !!!!!!!!!!!!!");
			CompanyVehicle updateCv = new CompanyVehicle();
			updateCv.setId(cv.getId());
			updateCv.setCompanyId(cv.getCompanyId());
			updateCv.setVehicleId(cv.getVehicleId());
			updateCv.setUserId(cv.getUserId());
			updateCv.setName(cv.getName());
			updateCv.setMemo(cv.getMemo());
			session.update(updateCv);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return CompanyUserVo !!!!!!!!!!!!!!!!!!!");
			future.complete(new CompanyVehicleVo(cv));
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
	@Operation(summary = "COMAPANY VEHICLE API - Delete")
	@DELETE
	@Path("/companyvehicle/{cvId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyVehicleVo> companyDelete(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("cvId") Long cvId) {

		CompletableFuture<CompanyVehicleVo> future = new CompletableFuture<>();

		if (cvId == null || cvId == 0) {
			request.response("Invalid Company Vehicle : " + cvId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		LOGGER.info("DELETE : /api/v1/companyvehicle/");

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
			CompanyVehicle aCv = session.find(CompanyVehicle.class, cvId);
			session.delete(aCv);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return Company !!!!!!!!!!!!!!!!!!!");
			future.complete(new CompanyVehicleVo(aCv));
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
