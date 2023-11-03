package kr.syszone.vk.be.sysop.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
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
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.CompanyApis;
import kr.syszone.vk.be.sysop.model.CompanyVo;
import kr.syszone.vk.be.sysop.model.ListHelper;
import kr.syszone.vk.be.sysop.model.SelectCompanyVo;

@Path("/api/v1/sysop")
public class CompanyService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompanyService.class);

	private CompanyApis companyApis;

	@JsonRequest
	@Operation(summary = "COMPANY API - Endpoint Information")
	@GET
	@Path("/company")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on Company!!");
		if (companyApis == null) {
			companyApis = new CompanyApis();
		}
		CompletableFuture<CompanyApis> future = new CompletableFuture<>();
		future.complete(companyApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "COMPANY API - List")
	@GET
	@Path("/company/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<CompanyVo>> getCompanyList(@Parameter @QueryParam("search") String search,
			@Parameter @QueryParam("order") String order, @Parameter @QueryParam("orderBy") String orderBy,
			@Parameter @QueryParam("page") Integer page, @Parameter @QueryParam("limit") Integer limit,
			@Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<CompanyVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/sysop/company/list");

		ListHelper<CompanyVo> listHelper = new ListHelper<CompanyVo>();
		List<CompanyVo> companyInfoList = new ArrayList<CompanyVo>();
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

			LOGGER.info("3) Querying Company Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery("SELECT COUNT(id) FROM Company WHERE id LIKE :keyword"
						+ " OR company_name LIKE :keyword" + " OR company_no LIKE :keyword" + " OR ceo LIKE :keyword"
						+ " OR postcode LIKE :keyword" + " OR address_post LIKE :keyword"
						+ " OR address_detail LIKE :keyword" + " OR phone_no LIKE :keyword"
						+ " OR homepage LIKE :keyword");
				countQuery.setParameter("keyword", '%' + search + '%');
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) FROM Company");
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();

			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying Company List !!!!!!!!!!!!!");
			Query<Company> listQuery = null;
			if (search != null && search.length() > 0) {
				listQuery = session.createQuery("FROM Company WHERE id LIKE :keyword" + " OR company_name LIKE :keyword"
						+ " OR company_no LIKE :keyword" + " OR ceo LIKE :keyword" + " OR postcode LIKE :keyword"
						+ " OR address_post LIKE :keyword" + " OR address_detail LIKE :keyword"
						+ " OR phone_no LIKE :keyword" + " OR homepage LIKE :keyword", Company.class);
				listQuery.setParameter("keyword", '%' + search + '%');
			} else {
				listQuery = session.createQuery("FROM Company", Company.class);
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			List<Company> result = listQuery.getResultList();

			Iterator<Company> it = result.iterator();
			while (it.hasNext()) {
				Company aCompany = it.next();
				companyInfoList.add(new CompanyVo(aCompany));
			}

			LOGGER.info("5) Return ComapnyInfo !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
			listHelper.setResult(companyInfoList);
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
	@Operation(summary = "Company API - Create")
	@POST
	@Path("/company")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<Company> companySignUp(@Parameter(hidden = true) RakamHttpRequest request,
			@RequestBody Company body) {

		LOGGER.info("POST : /api/v1/sysop/company/");
		CompletableFuture<Company> future = new CompletableFuture<>();

		LOGGER.info("Headers  Finished!! ");

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
	@Operation(summary = "Company API - Update")
	@PUT
	@Path("/company/{comId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyVo> companyUpdate(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("comId") String comId, @Parameter @RequestBody CompanyVo companyInfo) {

		CompletableFuture<CompanyVo> future = new CompletableFuture<>();
		if (comId == null || companyInfo == null || !comId.equals(companyInfo.getId())) {
			request.response("Invalid CompanyId : " + comId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Save Company Entity !!!!!!!!!!!!!");
			Company company = companyInfo.getCompany();
			session.update(company);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return CompanyVo !!!!!!!!!!!!!!!!!!!");
			future.complete(new CompanyVo(company));
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
	@Operation(summary = "Company API - Delete")
	@DELETE
	@Path("/company/{comId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<CompanyVo> companyDelete(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("comId") String ComId) {

		CompletableFuture<CompanyVo> future = new CompletableFuture<>();

		if (ComId == null || ComId.length() == 0) {
			request.response("Invalid CompanyId : " + ComId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		LOGGER.info("DELETE : /api/v1/sysop/company/");

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
			Company aCom = session.find(Company.class, ComId);
			session.delete(aCom);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return Company !!!!!!!!!!!!!!!!!!!");
			future.complete(new CompanyVo(aCom));
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
	@Operation(summary = "Company API - GeoCode")
	@GET
	@Path("/company/geocode")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public void getGeocode(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @QueryParam("apikey") String apiKey, @Parameter @QueryParam("queryaddr") String queryAddr) {

		String apiURL = "http://api.vworld.kr/req/address";
		if (apiKey == null | queryAddr == null | apiKey.length() < 10 | queryAddr.length() < 3) {
			request.response("Invalid ApiKey & RoadName : " + apiKey + " / " + queryAddr,
					HttpResponseStatus.BAD_REQUEST).end();
			return;

		}
		try {
			int responseCode = 0;
			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			String postParams = "service=address";
			postParams += "&request=getcoord";
			postParams += "&version=2.0";
			postParams += "&crs=EPSG:4326";
			postParams += "&address=" + URLEncoder.encode(queryAddr.toString(), "utf-8");
			postParams += "&refine=true";
			postParams += "&simple=false";
			postParams += "&format=json";
			postParams += "&type=road";
			postParams += "&errorFormat=json";
			postParams += "&key=" + apiKey;

			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postParams);
			wr.flush();
			wr.close();
			responseCode = con.getResponseCode();
			BufferedReader br;

			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}

			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}

			LOGGER.error("response : {} " + response);
			br.close();
			con.disconnect();

			request.response(response.toString(), HttpResponseStatus.OK).end();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@JsonRequest
	@Operation(summary = "Company API - Company Select List")
	@GET
	@Path("/companies")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<SelectCompanyVo>> getSelectCompanyList(
			@Parameter(hidden = true) RakamHttpRequest request, @Parameter @QueryParam("cn") String comName) {

		CompletableFuture<List<SelectCompanyVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/companies");

		List<SelectCompanyVo> companyList = new ArrayList<SelectCompanyVo>();

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

			if (comName != null) {
				if(comName.length() > 0) {
					listQuery = session.createQuery("FROM Company WHERE companyName LIKE :keyword", Company.class)
							.setParameter("keyword", "%" + comName + "%");
				}
			} else {
				listQuery = session.createQuery("FROM Company", Company.class);
			}

			List<Company> result = listQuery.getResultList();
			Iterator<Company> it = result.iterator();

			while (it.hasNext()) {
				SelectCompanyVo vo = new SelectCompanyVo();
				Company aCompany = it.next();
				vo.setCompanyId(aCompany.getId());
				vo.setCompanyName(aCompany.getCompanyName());
				companyList.add(vo);
			}

			LOGGER.info("5) Return SelectCompanyList !!!!!!!!!!!!!!!!!!!");
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

}
