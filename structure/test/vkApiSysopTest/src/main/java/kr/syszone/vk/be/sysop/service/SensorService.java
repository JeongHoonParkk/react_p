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
import org.hibernate.type.IntegerType;
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
import kr.syszone.vk.be.db.entity.Company;
import kr.syszone.vk.be.db.entity.Sensor;
import kr.syszone.vk.be.db.entity.SensorAssign;
import kr.syszone.vk.be.db.entity.SensorHistory;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.ListHelper;
import kr.syszone.vk.be.sysop.model.ListWrapperVo;
import kr.syszone.vk.be.sysop.model.SensorAddForCompanyVo;
import kr.syszone.vk.be.sysop.model.SensorApis;
import kr.syszone.vk.be.sysop.model.SensorAssignVo;
import kr.syszone.vk.be.sysop.model.SensorDataVo;
import kr.syszone.vk.be.sysop.model.SensorHistoryVo;

@Path("/api/v1/sysop")
public class SensorService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SensorService.class);

	private SensorApis sensorApis;

	@JsonRequest
	@Operation(summary = "Sensor API - Endpoint Information")
	@GET
	@Path("/sensor")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<SensorApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on USER!!");
		if (sensorApis == null) {
			sensorApis = new SensorApis();
		}
		CompletableFuture<SensorApis> future = new CompletableFuture<>();
		future.complete(sensorApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "Sensor API - Sensor List of Company")
	@GET
	@Path("/companies/{companyId}/sensors")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<SensorAssignVo>> getSensorListOfCompany(
			@Parameter(hidden = true) RakamHttpRequest request, @Parameter @PathParam("companyId") String companyId,
			@Parameter @QueryParam("search") String search, @Parameter @QueryParam("order") String order,
			@Parameter @QueryParam("orderBy") String orderBy, @Parameter @QueryParam("page") Integer page,
			@Parameter @QueryParam("limit") Integer limit) {

		CompletableFuture<ListHelper<SensorAssignVo>> future = new CompletableFuture<>();

		if (companyId == null) {
			request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		ListHelper<SensorAssignVo> listHelper = new ListHelper<SensorAssignVo>();
		List<SensorAssignVo> sensorAssignList = new ArrayList<SensorAssignVo>();

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

			LOGGER.info("3) Querying SensorAssign Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery("SELECT COUNT(a.id) " + // indent
						" FROM SensorAssign AS a " + // indent
						" LEFT JOIN Sensor AS b ON (a.sid = b.id) " + // indent
						" LEFT JOIN SensorInfo AS c ON (b.siId = c.id) " + // indent
						"WHERE a.companyId = :comId AND " + // indent
						"  (a.name LIKE :keyword OR a.memo LIKE :keyword OR " + // indent
						"  b.id LIKE :keyword OR b.memo LIKE :keyword OR " + // indent
						"  c.maker LIKE :keyword OR c.model LIKE :keyword)");
				countQuery.setParameter("comId", companyId);
				countQuery.setParameter("keyword", '%' + search + '%');
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) " + // indent
						"FROM SensorAssign " + // indent
						"WHERE companyId = :comId");
				countQuery.setParameter("comId", companyId);
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();

			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying SensorAssign List !!!!!!!!!!!!!");
			Query<?> listQuery = null;
			if (search != null && search.length() > 0) {
				listQuery = session.createSQLQuery("SELECT " + // indent
						"    a.*, " + // indent
						"    b.si_id AS siId, " + // indent
						"    b.memo AS sensorMemo, " + // indent
						"    c.id AS sensorInfoId, " + // indent
						"    c.maker AS sensorMaker, " + // indent
						"    c.model AS sensorModel, " + // indent
						"    c.net_type AS netType, " + // indent
						"    c.data_type AS dataType " + // indent
						" FROM sensor_assign AS a " + // indent
						" LEFT JOIN sensor AS b ON (a.sid = b.id) " + // indent
						" LEFT JOIN sensor_info AS c ON (b.si_id = c.id) " + // indent
						"WHERE a.company_id = :comId AND " + // indent
						"  (a.name LIKE :keyword OR a.memo LIKE :keyword OR " + // indent
						"  b.id LIKE :keyword OR b.memo LIKE :keyword OR " + // indent
						"  c.maker LIKE :keyword OR c.model LIKE :keyword)") // indent
						.addEntity("sensor_assign", SensorAssign.class) // indent
						.addScalar("siId", new LongType()) // indent
						.addScalar("sensorMemo", new StringType()) // indent
						.addScalar("sensorInfoId", new LongType()) // indent
						.addScalar("sensorMaker", new StringType()) // indent
						.addScalar("sensorModel", new StringType()) // indent
						.addScalar("netType", new IntegerType()) // indent
						.addScalar("dataType", new IntegerType()); // indent
				listQuery.setParameter("comId", companyId);
				listQuery.setParameter("keyword", '%' + search + '%');
			} else {
				listQuery = session.createSQLQuery("SELECT " + // indent
						"    a.*, " + // indent
						"    b.si_id AS siId, " + // indent
						"    b.memo AS sensorMemo, " + // indent
						"    c.id AS sensorInfoId, " + // indent
						"    c.maker AS sensorMaker, " + // indent
						"    c.model AS sensorModel, " + // indent
						"    c.net_type AS netType, " + // indent
						"    c.data_type AS dataType " + // indent
						" FROM sensor_assign AS a " + // indent
						" LEFT JOIN sensor AS b ON (a.sid = b.id) " + // indent
						" LEFT JOIN sensor_info AS c ON (b.si_id = c.id) " + // indent
						"WHERE a.company_id = :comId") // indent
						.addEntity("sensor_assign", SensorAssign.class) // indent
						.addScalar("siId", new LongType()) // indent
						.addScalar("sensorMemo", new StringType()) // indent
						.addScalar("sensorInfoId", new LongType()) // indent
						.addScalar("sensorMaker", new StringType()) // indent
						.addScalar("sensorModel", new StringType()) // indent
						.addScalar("netType", new IntegerType()) // indent
						.addScalar("dataType", new IntegerType()); // indent
				listQuery.setParameter("comId", companyId);
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			@SuppressWarnings("unchecked")
			List<Object[]> result = (List<Object[]>) listQuery.list();
			result.stream().forEach((record) -> {
				SensorAssign aRow = (SensorAssign) record[0];
				Long siId = (Long) record[1];
				String sensorMemo = (String) record[2];
				Long sensorInfoId = (Long) record[3];
				String sensorMaker = (String) record[4];
				String sensorModel = (String) record[5];
				Integer netType = (Integer) record[6];
				Integer dataType = (Integer) record[7];
				sensorAssignList.add(new SensorAssignVo(aRow, siId, sensorMemo, sensorInfoId, sensorMaker, sensorModel,
						netType, dataType));
			});

			LOGGER.info("5) Return SensorInfo !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
			listHelper.setResult(sensorAssignList);
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
	@Operation(summary = "Sensor API - Create Sensors for Company")
	@POST
	@Path("/companies/{companyId}/sensors")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListWrapperVo<SensorAddForCompanyVo>> postSensorForCompany(
			@Parameter(hidden = true) RakamHttpRequest request, @Parameter @PathParam("companyId") String companyId,
			@Parameter @RequestBody ListWrapperVo<SensorAddForCompanyVo> reqSensorList) {
		CompletableFuture<ListWrapperVo<SensorAddForCompanyVo>> future = new CompletableFuture<>();

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

			LOGGER.info("4) Save SensorInfo Entity !!!!!!!!!!!!!");
			for (SensorAddForCompanyVo safc : reqSensorList.getDataList()) {
				Sensor aRow = new Sensor();
				aRow.setId(safc.getSensorId());
				aRow.setSiId(safc.getSensorInfoId());
				aRow.setMemo(safc.getSensorName());
				session.save(aRow);

				SensorAssign sa = new SensorAssign();
				sa.setSid(aRow.getId());
				sa.setName(safc.getSensorName());
				sa.setCompanyId(companyId);
				session.save(sa);

				SensorHistory sh = new SensorHistory();
				sh.setSid(aRow.getId());
				sh.setCompanyId(companyId);
				sh.setWork(safc.getWork());
				sh.setTsWork(new Timestamp(System.currentTimeMillis()));
				session.save(sh);
			}

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return SensorAddForCompanyVo !!!!!!!!!!!!!!!!!!!");
			future.complete(reqSensorList);
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
	@Operation(summary = "Sensor API - Modify the Sensor Assignment for Company")
	@PUT
	@Path("/companies/{companyId}/sensors/{sensorAssignId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<SensorAssignVo> putSensorAssignForCompany(
			@Parameter(hidden = true) RakamHttpRequest request, @Parameter @PathParam("companyId") String companyId,
			@Parameter @PathParam("sensorAssignId") Long sensorAssignId,
			@Parameter @RequestBody SensorAssignVo reqSensorAssign) {
		CompletableFuture<SensorAssignVo> future = new CompletableFuture<>();

		if (companyId == null || companyId.length() == 0) {
			request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (sensorAssignId == null || sensorAssignId <= 0) {
			request.response("Invalid sensorAssignId : " + sensorAssignId, HttpResponseStatus.BAD_REQUEST).end();
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

			SensorAssign sa = session.find(SensorAssign.class, sensorAssignId);
			if (sa == null) {
				request.response("Invalid sensorAssignId : " + sensorAssignId, HttpResponseStatus.BAD_REQUEST).end();
				return future;
			}

			LOGGER.info("4) Modify SensorAssign Entity !!!!!!!!!!!!!");
			sa.setName(reqSensorAssign.getName());
			sa.setMemo(reqSensorAssign.getMemo());
			session.update(sa);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return SensorAddForCompanyVo !!!!!!!!!!!!!!!!!!!");
			future.complete(new SensorAssignVo(sa));
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
	@Operation(summary = "Sensor API - Delete the Sensor Assignment for Company")
	@DELETE
	@Path("/companies/{companyId}/sensors/{sensorAssignId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<SensorAssignVo> deleteSensorAssignForCompany(
			@Parameter(hidden = true) RakamHttpRequest request, @Parameter @PathParam("companyId") String companyId,
			@Parameter @PathParam("sensorAssignId") Long sensorAssignId) {
		CompletableFuture<SensorAssignVo> future = new CompletableFuture<>();

		if (companyId == null || companyId.length() == 0) {
			request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (sensorAssignId == null || sensorAssignId <= 0) {
			request.response("Invalid sensorAssignId : " + sensorAssignId, HttpResponseStatus.BAD_REQUEST).end();
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

			SensorAssign sa = session.find(SensorAssign.class, sensorAssignId);
			if (sa == null) {
				request.response("Invalid sensorAssignId : " + sensorAssignId, HttpResponseStatus.BAD_REQUEST).end();
				return future;
			}

			SensorHistory sh = new SensorHistory();
			sh.setSid(sa.getSid());
			sh.setCompanyId(companyId);
			sh.setWork("센서 보유 현황 삭제 : " + sa.getSid());
			sh.setTsWork(new Timestamp(System.currentTimeMillis()));
			session.save(sh);

			LOGGER.info("4) Delete SensorAssign Entity !!!!!!!!!!!!!");
			session.delete(sa);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return SensorAddForCompanyVo !!!!!!!!!!!!!!!!!!!");
			future.complete(new SensorAssignVo(sa));
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
	@Operation(summary = "Sensor API - Sensor Data")
	@GET
	@Path("/companies/{companyId}/sensors/{sensorId}/data")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<SensorDataVo>> getSensorData(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("companyId") String companyId, @Parameter @PathParam("sensorId") String sensorId,
			@Parameter @QueryParam("sd") String sd, @Parameter(required = false) @QueryParam("ed") String ed) {

		CompletableFuture<List<SensorDataVo>> future = new CompletableFuture<>();

		if (companyId == null || companyId.length() == 0) {
			request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (sensorId == null || sensorId.length() == 0) {
			request.response("Invalid SensorId : " + sensorId, HttpResponseStatus.BAD_REQUEST).end();
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
					if (fluxRecord.getValueByKey("storeTime") != null) {
						sl.setStoreTime(new Timestamp(
								Instant.parse(fluxRecord.getValueByKey("storeTime").toString()).toEpochMilli()));
					}
					sl.setTemperature((Double) fluxRecord.getValueByKey("temperature"));
					sl.setHumidity((Double) fluxRecord.getValueByKey("humidity"));
					sl.setPressure((Double) fluxRecord.getValueByKey("pressure"));
					sl.setLight((Double) fluxRecord.getValueByKey("light"));
					sl.setDoorState((Long) fluxRecord.getValueByKey("doorState"));
					sl.setVoltage((Double) fluxRecord.getValueByKey("voltage"));

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
	@Operation(summary = "Sensor API - Sensor History")
	@GET
	@Path("/companies/{companyId}/sensors/{sensorId}/history")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<SensorHistoryVo>> getSensorHistory(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("companyId") String companyId, @Parameter @PathParam("sensorId") String sensorId) {

		CompletableFuture<List<SensorHistoryVo>> future = new CompletableFuture<>();

		if (companyId == null || companyId.length() == 0) {
			request.response("Invalid CompanyId : " + companyId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}
		if (sensorId == null || companyId.length() == 0) {
			request.response("Invalid SensorId : " + sensorId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		Session session = null;
		try {
			List<SensorHistoryVo> shList = new ArrayList<SensorHistoryVo>();

			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Query the History of Sensor!!!!");
			Query<SensorHistory> listQuery = session.createQuery( // indent
					"FROM SensorHistory " + // indent
							"WHERE sid=:sId AND " + // indent
							"      companyId=:cId", // indent
					SensorHistory.class);
			listQuery.setParameter("sId", sensorId);
			listQuery.setParameter("cId", companyId);

			List<SensorHistory> result = listQuery.getResultList();

			Iterator<SensorHistory> it = result.iterator();
			while (it.hasNext()) {
				SensorHistory aRow = it.next();
				shList.add(new SensorHistoryVo(aRow));
			}

			LOGGER.info("4) Return SensorHistoryVo !!!!!!!!!!!!!!!!!!!");
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
