package kr.syszone.vk.be.sysop.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
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

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

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
import kr.syszone.vk.be.db.InfluxDbUtil;
import kr.syszone.vk.be.db.entity.Aircon;
import kr.syszone.vk.be.db.entity.AirconAssign;
import kr.syszone.vk.be.db.entity.AirconHistory;
import kr.syszone.vk.be.db.entity.Company;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.AirconAddForCompanyVo;
import kr.syszone.vk.be.sysop.model.AirconApis;
import kr.syszone.vk.be.sysop.model.AirconAssignVo;
import kr.syszone.vk.be.sysop.model.AirconDataVo;
import kr.syszone.vk.be.sysop.model.AirconHistoryVo;
import kr.syszone.vk.be.sysop.model.ListHelper;
import kr.syszone.vk.be.sysop.model.ListWrapperVo;

@Path("/api/v1/sysop")
public class AirconService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AirconService.class);

	private AirconApis airconApis;

	@JsonRequest
	@Operation(summary = "Aircon API - Endpoint Information")
	@GET
	@Path("/aircon")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<AirconApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on USER!!");
		if (airconApis == null) {
			airconApis = new AirconApis();
		}
		CompletableFuture<AirconApis> future = new CompletableFuture<>();
		future.complete(airconApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "Aircon API - Aircon List of Company")
	@GET
	@Path("/companies/{companyId}/aircons")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<AirconAssignVo>> getAirconListOfCompany(
			@Parameter(hidden = true) RakamHttpRequest request, @Parameter @PathParam("companyId") String companyId,
			@Parameter @QueryParam("search") String search, @Parameter @QueryParam("order") String order,
			@Parameter @QueryParam("orderBy") String orderBy, @Parameter @QueryParam("page") Integer page,
			@Parameter @QueryParam("limit") Integer limit) {

		CompletableFuture<ListHelper<AirconAssignVo>> future = new CompletableFuture<>();

		if (companyId == null) {
			request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		ListHelper<AirconAssignVo> listHelper = new ListHelper<AirconAssignVo>();
		List<AirconAssignVo> airconAssignList = new ArrayList<AirconAssignVo>();

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

			LOGGER.info("3) Querying AirconAssign Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery("SELECT COUNT(a.id) " + // indent
						" FROM AirconAssign AS a " + // indent
						" LEFT JOIN Aircon AS b ON (a.aid = b.id) " + // indent
						" LEFT JOIN AirconInfo AS c ON (b.aiId = c.id) " + // indent
						"WHERE a.companyId = :comId AND " + // indent
						"  (a.name LIKE :keyword OR a.memo LIKE :keyword OR " + // indent
						"  b.id LIKE :keyword OR b.memo LIKE :keyword OR " + // indent
						"  c.maker LIKE :keyword OR c.model LIKE :keyword)");
				countQuery.setParameter("comId", companyId);
				countQuery.setParameter("keyword", '%' + search + '%');
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) " + // indent
						"FROM AirconAssign " + // indent
						"WHERE companyId = :comId");
				countQuery.setParameter("comId", companyId);
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();

			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying AirconAssign List !!!!!!!!!!!!!");
			Query<?> listQuery = null;
			if (search != null && search.length() > 0) {
				listQuery = session.createSQLQuery("SELECT " + // indent
						"    a.*, " + // indent
						"    b.ai_id AS aiId, " + // indent
						"    b.memo AS airconMemo, " + // indent
						"    c.id AS airconInfoId, " + // indent
						"    c.maker AS airconMaker, " + // indent
						"    c.model AS airconModel " + // indent
						" FROM aircon_assign AS a " + // indent
						" LEFT JOIN aircon AS b ON (a.aid = b.id) " + // indent
						" LEFT JOIN aircon_info AS c ON (b.ai_id = c.id) " + // indent
						"WHERE a.company_id = :comId AND " + // indent
						"  (a.name LIKE :keyword OR a.memo LIKE :keyword OR " + // indent
						"  b.id LIKE :keyword OR b.memo LIKE :keyword OR " + // indent
						"  c.maker LIKE :keyword OR c.model LIKE :keyword)") // indent
						.addEntity("aircon_assign", AirconAssign.class) // indent
						.addScalar("aiId", new LongType()) // indent
						.addScalar("airconMemo", new StringType()) // indent
						.addScalar("airconInfoId", new LongType()) // indent
						.addScalar("airconMaker", new StringType()) // indent
						.addScalar("airconModel", new StringType()); // indent
				listQuery.setParameter("comId", companyId);
				listQuery.setParameter("keyword", '%' + search + '%');
			} else {
				listQuery = session.createSQLQuery("SELECT " + // indent
						"    a.*, " + // indent
						"    b.ai_id AS aiId, " + // indent
						"    b.memo AS airconMemo, " + // indent
						"    c.id AS airconInfoId, " + // indent
						"    c.maker AS airconMaker, " + // indent
						"    c.model AS airconModel " + // indent
						" FROM aircon_assign AS a " + // indent
						" LEFT JOIN aircon AS b ON (a.aid = b.id) " + // indent
						" LEFT JOIN aircon_info AS c ON (b.ai_id = c.id) " + // indent
						"WHERE a.company_id = :comId") // indent
						.addEntity("aircon_assign", AirconAssign.class) // indent
						.addScalar("aiId", new LongType()) // indent
						.addScalar("airconMemo", new StringType()) // indent
						.addScalar("airconInfoId", new LongType()) // indent
						.addScalar("airconMaker", new StringType()) // indent
						.addScalar("airconModel", new StringType()); // indent
				listQuery.setParameter("comId", companyId);
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			@SuppressWarnings("unchecked")
			List<Object[]> result = (List<Object[]>) listQuery.list();
			result.stream().forEach((record) -> {
				AirconAssign aRow = (AirconAssign) record[0];
				Long aiId = (Long) record[1];
				String airconMemo = (String) record[2];
				Long airconInfoId = (Long) record[3];
				String airconMaker = (String) record[4];
				String airconModel = (String) record[5];
				airconAssignList
						.add(new AirconAssignVo(aRow, aiId, airconMemo, airconInfoId, airconMaker, airconModel));
			});

			LOGGER.info("5) Return AirconInfo !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
			listHelper.setResult(airconAssignList);
			future.complete(listHelper);
		} catch (Exception e) {
			LOGGER.error("Db Operation Failed !!!!!!!!!!!!!", e);
			request.response(e.getLocalizedMessage(), HttpResponseStatus.BAD_REQUEST).end();
		} finally {
			LOGGER.info("6) Close Session !!!!!!!!!!!!!!!!!");
			if (session != null) {
				session.close();
			}
		}
		return future;
	}

	@JsonRequest
	@Operation(summary = "Aircon API - Create Aircons for Company")
	@POST
	@Path("/companies/{companyId}/aircons")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListWrapperVo<AirconAddForCompanyVo>> postAirconForCompany(
			@Parameter(hidden = true) RakamHttpRequest request, @Parameter @PathParam("companyId") String companyId,
			@Parameter @RequestBody ListWrapperVo<AirconAddForCompanyVo> reqAirconList) {
		CompletableFuture<ListWrapperVo<AirconAddForCompanyVo>> future = new CompletableFuture<>();

		if (companyId == null) {
			request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
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

			Company c = session.find(Company.class, companyId);
			if (c == null) {
				request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
				return future;
			}

			LOGGER.info("4) Save AirconInfo Entity !!!!!!!!!!!!!");
			for (AirconAddForCompanyVo safc : reqAirconList.getDataList()) {
				Aircon aRow = new Aircon();
				aRow.setId(safc.getAirconId());
				aRow.setAiId(safc.getAirconInfoId());
				aRow.setMemo(safc.getAirconName());
				session.save(aRow);

				AirconAssign aa = new AirconAssign();
				aa.setAid(aRow.getId());
				aa.setName(safc.getAirconName());
				aa.setCompanyId(companyId);
				session.save(aa);

				AirconHistory ah = new AirconHistory();
				ah.setAid(aRow.getId());
				ah.setCompanyId(companyId);
				ah.setWork(safc.getWork());
				ah.setTsWork(new Timestamp(System.currentTimeMillis()));
				session.save(ah);
			}

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return AirconAddForCompanyVo !!!!!!!!!!!!!!!!!!!");
			future.complete(reqAirconList);
		} catch (Exception e) {
			LOGGER.error("Db Operation Failed !!!!!!!!!!!!!", e);
			request.response(e.getLocalizedMessage(), HttpResponseStatus.BAD_REQUEST).end();
		} finally {
			LOGGER.info("6) Close Session !!!!!!!!!!!!!!!!!");
			if (session != null) {
				session.close();
			}
		}
		return future;
	}

	@JsonRequest
	@Operation(summary = "Aircon API - Modify the Aircon Assignment for Company")
	@PUT
	@Path("/companies/{companyId}/aircons/{airconAssignId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<AirconAssignVo> putAirconAssignForCompany(
			@Parameter(hidden = true) RakamHttpRequest request, @Parameter @PathParam("companyId") String companyId,
			@Parameter @PathParam("airconAssignId") Long airconAssignId,
			@Parameter @RequestBody AirconAssignVo reqAirconAssign) {
		CompletableFuture<AirconAssignVo> future = new CompletableFuture<>();

		if (companyId == null || companyId.length() == 0) {
			request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (airconAssignId == null || airconAssignId <= 0) {
			request.response("Invalid airconAssignId : " + airconAssignId, HttpResponseStatus.BAD_REQUEST).end();
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

			AirconAssign aa = session.find(AirconAssign.class, airconAssignId);
			if (aa == null) {
				request.response("Invalid airconAssignId : " + airconAssignId, HttpResponseStatus.BAD_REQUEST).end();
				return future;
			}

			LOGGER.info("4) Modify AirconAssign Entity !!!!!!!!!!!!!");
			aa.setName(reqAirconAssign.getName());
			aa.setMemo(reqAirconAssign.getMemo());
			session.update(aa);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return AirconAddForCompanyVo !!!!!!!!!!!!!!!!!!!");
			future.complete(new AirconAssignVo(aa));
		} catch (Exception e) {
			LOGGER.error("Db Operation Failed !!!!!!!!!!!!!", e);
			request.response(e.getLocalizedMessage(), HttpResponseStatus.BAD_REQUEST).end();
		} finally {
			LOGGER.info("6) Close Session !!!!!!!!!!!!!!!!!");
			if (session != null) {
				session.close();
			}
		}
		return future;
	}

	@JsonRequest
	@Operation(summary = "Aircon API - Delete the Aircon Assignment for Company")
	@DELETE
	@Path("/companies/{companyId}/aircons/{airconAssignId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<AirconAssignVo> deleteAirconAssignForCompany(
			@Parameter(hidden = true) RakamHttpRequest request, @Parameter @PathParam("companyId") String companyId,
			@Parameter @PathParam("airconAssignId") Long airconAssignId) {
		CompletableFuture<AirconAssignVo> future = new CompletableFuture<>();

		if (companyId == null || companyId.length() == 0) {
			request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (airconAssignId == null || airconAssignId <= 0) {
			request.response("Invalid airconAssignId : " + airconAssignId, HttpResponseStatus.BAD_REQUEST).end();
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

			AirconAssign aa = session.find(AirconAssign.class, airconAssignId);
			if (aa == null) {
				request.response("Invalid airconAssignId : " + airconAssignId, HttpResponseStatus.BAD_REQUEST).end();
				return future;
			}

			AirconHistory ah = new AirconHistory();
			ah.setAid(aa.getAid());
			ah.setCompanyId(companyId);
			ah.setWork("공조기기 보유 현황 삭제 : " + aa.getAid());
			ah.setTsWork(new Timestamp(System.currentTimeMillis()));
			session.save(ah);

			LOGGER.info("4) Delete AirconAssign Entity !!!!!!!!!!!!!");
			session.delete(aa);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return AirconAddForCompanyVo !!!!!!!!!!!!!!!!!!!");
			future.complete(new AirconAssignVo(aa));
		} catch (Exception e) {
			LOGGER.error("Db Operation Failed !!!!!!!!!!!!!", e);
			request.response(e.getLocalizedMessage(), HttpResponseStatus.BAD_REQUEST).end();
		} finally {
			LOGGER.info("7) Close Session !!!!!!!!!!!!!!!!!");
			if (session != null) {
				session.close();
			}
		}
		return future;
	}

	@JsonRequest
	@Operation(summary = "Aircon API - Aircon Data")
	@GET
	@Path("/companies/{companyId}/aircons/{airconId}/data")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<AirconDataVo>> getAirconData(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("companyId") String companyId, @Parameter @PathParam("airconId") String airconId,
			@Parameter @QueryParam("sd") String sd, @Parameter(required = false) @QueryParam("ed") String ed) {

		CompletableFuture<List<AirconDataVo>> future = new CompletableFuture<>();

		if (companyId == null || companyId.length() == 0) {
			request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (airconId == null || companyId.length() == 0) {
			request.response("Invalid AirconId : " + airconId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (sd == null || sd.length() < 10) {
			request.response("Invalid startDate : " + sd, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		try {
			InfluxDBClient dbClient = InfluxDbUtil.getClient();
			QueryApi qa = dbClient.getQueryApi();

			String ssd = sd + "T00:00:00.000+09:00";
			String sed;
			if (ed == null || ed.length() == 0) {
				sed = sd + "T23:59:59.999+09:00";
			} else {
				sed = ed + "T23:59:59.999+09:00";
			}

			StringBuilder sb = new StringBuilder();
			sb.append("from(bucket: \"valuekeeper\")").append('\n');
			sb.append("  |> range(start: ").append(ssd).append(',');
			sb.append("           stop: ").append(sed).append(')').append('\n');
			sb.append("  |> filter(fn: (r) => r[\"_measurement\"] == \"airconRunLog\" and ");
			sb.append("                       r[\"aid\"] == \"").append(airconId).append("\")\n");
			sb.append("  |> pivot(rowKey: [\"_time\", \"aid\"], columnKey: [\"_field\"], valueColumn: \"_value\")\n");
			sb.append("  |> sort(columns: [\"_time\"])\n");

			LOGGER.info("InfluxQL : \n{}", sb.toString());

			List<AirconDataVo> slList = new ArrayList<AirconDataVo>();
			List<FluxTable> tables = qa.query(sb.toString());
			for (FluxTable fluxTable : tables) {
				List<FluxRecord> records = fluxTable.getRecords();
				for (FluxRecord fluxRecord : records) {
					AirconDataVo sl = new AirconDataVo();

					sl.setAid((String) fluxRecord.getValueByKey("aid"));
					sl.setTime(
							new Timestamp(Instant.parse(fluxRecord.getValueByKey("_time").toString()).toEpochMilli()));
					sl.setStoreTime(new Timestamp(
							Instant.parse(fluxRecord.getValueByKey("storeTime").toString()).toEpochMilli()));
					sl.setCfgTemp((Double) fluxRecord.getValueByKey("cfg_temp"));
					sl.setRunMode(Integer.parseInt(fluxRecord.getValueByKey("run_mode").toString()));

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
	@Operation(summary = "Aircon API - Aircon History")
	@GET
	@Path("/companies/{companyId}/aircons/{airconId}/history")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<AirconHistoryVo>> getAirconHistory(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("companyId") String companyId, @Parameter @PathParam("airconId") String airconId) {

		CompletableFuture<List<AirconHistoryVo>> future = new CompletableFuture<>();

		if (companyId == null || companyId.length() == 0) {
			request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (airconId == null || companyId.length() == 0) {
			request.response("Invalid AirconId : " + airconId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		Session session = null;
		try {
			List<AirconHistoryVo> shList = new ArrayList<AirconHistoryVo>();

			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Query the History of Aircon!!!!");
			Query<AirconHistory> listQuery = session.createQuery( // indent
					"FROM AirconHistory " + // indent
							"WHERE aid=:aId AND " + // indent
							"      companyId=:cId", // indent
					AirconHistory.class);
			listQuery.setParameter("aId", airconId);
			listQuery.setParameter("cId", companyId);

			List<AirconHistory> result = listQuery.getResultList();

			Iterator<AirconHistory> it = result.iterator();
			while (it.hasNext()) {
				AirconHistory aRow = it.next();
				shList.add(new AirconHistoryVo(aRow));
			}

			LOGGER.info("4) Return AirconHistoryVo !!!!!!!!!!!!!!!!!!!");
			future.complete(shList);
		} catch (Exception e) {
			LOGGER.error("Db Operation Failed !!!!!!!!!!!!!", e);
			request.response(e.getLocalizedMessage(), HttpResponseStatus.BAD_REQUEST).end();
		} finally {
			LOGGER.info("5) Close Session !!!!!!!!!!!!!!!!!");
			if (session != null) {
				session.close();
			}
		}
		return future;
	}

}
