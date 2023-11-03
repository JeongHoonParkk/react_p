package kr.syszone.vk.be.sysop.service;

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
import kr.syszone.vk.be.db.entity.Company;
import kr.syszone.vk.be.db.entity.Vehicle;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.SelectCompanyVehicleVo;
import kr.syszone.vk.be.sysop.model.VehicleApis;
import kr.syszone.vk.be.sysop.model.VehicleVo;

@Path("/api/v1/sysop/test")
public class TestService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestService.class);

	private VehicleApis VehicleApis;

	@JsonRequest
	@Operation(summary = "Vehicle API - Endpoint Information")
	@GET
	@Path("/vehicle")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<VehicleApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on Vehicle!!");
		if (VehicleApis == null) {
			VehicleApis = new VehicleApis();
		}
		CompletableFuture<VehicleApis> future = new CompletableFuture<>();
		future.complete(VehicleApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "Vehicle API - List")
	@GET
	@Path("/vehicle/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<Company> getVehicleList(@Parameter(hidden = true) RakamHttpRequest request) { // 받
      
		LOGGER.info("GET : /api/v1/sysop/test/vehicle/list");
		CompletableFuture<Company> future = new CompletableFuture<Company>();
		Session session = null;
		try {
			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();
			session = sf.openSession();
			
			Query<Company> query = session.createQuery("From Company WHERE id = :id ",Company.class).setParameter("id", "cg");
			
			Company companyEntity = query.getSingleResult();
			
			future.complete(companyEntity);
			
			
		} catch (Exception e) {	
			LOGGER.error("Db Operation Failed !!!!!!!!!!!!!", e);
		} finally {
			LOGGER.info("6) 세선 닫힘");
			if (session != null) {
				session.close();
			}
		}
		return future;
	}

	@JsonRequest
	@Operation(summary = "Vehicle API - Create")
	@POST
	@Path("/vehicle")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<Vehicle> VehicleCreate(@Parameter(hidden = true) RakamHttpRequest request,
			@RequestBody Vehicle body) {

		LOGGER.info("POST : /api/v1/sysop/vehicle/");
		CompletableFuture<Vehicle> future = new CompletableFuture<>();

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
	@Operation(summary = "Vehicle API - Update")
	@PUT
	@Path("/vehicle/{vehicleId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<VehicleVo> VehicleUpdate(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("vehicleId") String vehicleId, @Parameter @RequestBody VehicleVo vehicleVo) {

		CompletableFuture<VehicleVo> future = new CompletableFuture<>();
		if (vehicleId == null || vehicleVo == null || (Long.parseLong(vehicleId) != vehicleVo.getId())) {
			request.response("Invalid VehicleId : " + vehicleId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Save Vehicle Entity !!!!!!!!!!!!!");
			Vehicle vehicle = vehicleVo.getVehicle();
			session.update(vehicle);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return VehicleVo !!!!!!!!!!!!!!!!!!!");
			future.complete(new VehicleVo(vehicle));
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
	@Operation(summary = "Vehicle API - Delete")
	@DELETE
	@Path("/vehicle/{vehicleid}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<VehicleVo> VehicleDelete(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("vehicleid") Long vehicleId) {

		CompletableFuture<VehicleVo> future = new CompletableFuture<>();

		if (vehicleId == null || vehicleId == 0) {
			request.response("Invalid VehicleId : " + vehicleId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		LOGGER.info("DELETE : /api/v1/sysop/vehicle");

		Session session = null;
		Transaction transaction = null;
		try {
			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Begin Transaction !!!!!!!!!!!!!");
			transaction = session.beginTransaction();

			LOGGER.info("4) Delete Vehicle !!!!!!!!!!!!!!!!!!!");
			Vehicle aVehicle = session.find(Vehicle.class, vehicleId);
			session.delete(aVehicle);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return Vehicle !!!!!!!!!!!!!!!!!!!");
			future.complete(new VehicleVo(aVehicle));
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
	@Operation(summary = "Vehicle API - Company Select List")
	@GET
	@Path("/vehicles")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<SelectCompanyVehicleVo>> getSelectVehicleList(
			@Parameter(hidden = true) RakamHttpRequest request, @Parameter @QueryParam("vNo") String vNo) {

		CompletableFuture<List<SelectCompanyVehicleVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/vehicles");

		List<SelectCompanyVehicleVo> vehicleList = new ArrayList<SelectCompanyVehicleVo>();

		Session session = null;

		try {

			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Querying vehicles Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			countQuery = session.createQuery("SELECT COUNT(id) FROM Vehicle");
			Long rowsTotal = (Long) countQuery.uniqueResult();
			if (rowsTotal == 0) {
				request.response("Vehicle is Empty : ", HttpResponseStatus.BAD_REQUEST).end();
				return future;
			}

			LOGGER.info("4) Querying vehicles List !!!!!!!!!!!!!");
			Query<Vehicle> listQuery = null;
			listQuery = session.createQuery("FROM Vehicle WHERE carNo LIKE :vNo", Vehicle.class).setParameter("vNo",
					"%" + vNo + "%");
			List<Vehicle> result = listQuery.getResultList();
			Iterator<Vehicle> it = result.iterator();

			while (it.hasNext()) {
				SelectCompanyVehicleVo vo = new SelectCompanyVehicleVo();
				Vehicle aVehicle = it.next();
				vo.setVehicleId(aVehicle.getId());
				vo.setVehicleNo(aVehicle.getCarNo());
				vehicleList.add(vo);
			}

			LOGGER.info("5) Return Select Vehicles List !!!!!!!!!!!!!!!!!!!");
			future.complete(vehicleList);
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
	@Operation(summary = "Vehicle API - Vehicle All List")
	@GET
	@Path("/vehicle/select")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<SelectCompanyVehicleVo>> getAllVehicleList(
			@Parameter(hidden = true) RakamHttpRequest request) {

		CompletableFuture<List<SelectCompanyVehicleVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/vehicles");

		List<SelectCompanyVehicleVo> vehicleList = new ArrayList<SelectCompanyVehicleVo>();

		Session session = null;

		try {

			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Querying vehicles Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			countQuery = session.createQuery("SELECT COUNT(id) FROM Vehicle");
			Long rowsTotal = (Long) countQuery.uniqueResult();
			if (rowsTotal == 0) {
				request.response("Vehicle is Empty : ", HttpResponseStatus.BAD_REQUEST).end();
				return future;
			}

			LOGGER.info("4) Querying vehicles List !!!!!!!!!!!!!");
			Query<?> listQuery = null;
			listQuery = session
					.createSQLQuery("SELECT v.*, cv.name as vName FROM vehicle as v"
							+ " LEFT JOIN company_vehicle cv ON (cv.vehicle_id = v.id)")
					.addEntity(Vehicle.class).addScalar("vName", new StringType());
			@SuppressWarnings("unchecked")
			List<Object[]> result = (List<Object[]>) listQuery.list();
			result.stream().forEach((record) -> {
				Vehicle aVehicle = (Vehicle) record[0];
				String vName = (String) record[1];
				vehicleList.add(new SelectCompanyVehicleVo(aVehicle, vName));
			});

			LOGGER.info("5) Return All Vehicles List !!!!!!!!!!!!!!!!!!!");
			future.complete(vehicleList);
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
