package kr.syszone.vk.be.sysop.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import kr.syszone.vk.be.db.entity.FileBase;
import kr.syszone.vk.be.db.entity.SensorInfo;
import kr.syszone.vk.be.db.entity.SensorInfoFiles;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.FileDeleteHelper;
import kr.syszone.vk.be.sysop.model.FileVo;
import kr.syszone.vk.be.sysop.model.ListHelper;
import kr.syszone.vk.be.sysop.model.SensorInfoApis;
import kr.syszone.vk.be.sysop.model.SensorInfoVo;

@Path("/api/v1/sysop")
public class SensorInfoService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SensorInfoService.class);

	private SensorInfoApis sensorInfoApis;

	@JsonRequest
	@Operation(summary = "SensorInfo API - Endpoint Information")
	@GET
	@Path("/sensorInfo")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<SensorInfoApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on USER!!");
		if (sensorInfoApis == null) {
			sensorInfoApis = new SensorInfoApis();
		}
		CompletableFuture<SensorInfoApis> future = new CompletableFuture<>();
		future.complete(sensorInfoApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "SensorInfo API - List")
	@GET
	@Path("/sensorInfo/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<SensorInfoVo>> getSensorInfoList(@Parameter @QueryParam("search") String search,
			@Parameter @QueryParam("order") String order, @Parameter @QueryParam("orderBy") String orderBy,
			@Parameter @QueryParam("page") Integer page, @Parameter @QueryParam("limit") Integer limit,
			@Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<SensorInfoVo>> future = new CompletableFuture<>();
		ListHelper<SensorInfoVo> listHelper = new ListHelper<SensorInfoVo>();
		List<SensorInfoVo> sensorInfoList = new ArrayList<SensorInfoVo>();
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

			LOGGER.info("3) Querying SensorInfo Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery(
						"SELECT COUNT(id) FROM SensorInfo WHERE maker LIKE :keyword OR model LIKE :keyword OR maker_link LIKE :keyword OR model_link LIKE :keyword OR memo LIKE :keyword");
				countQuery.setParameter("keyword", '%' + search + '%');
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) FROM SensorInfo");
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();

			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying SensorInfo List !!!!!!!!!!!!!");
			Query<SensorInfo> listQuery = null;
			if (search != null && search.length() > 0) {
				listQuery = session.createQuery(
						"FROM SensorInfo WHERE maker LIKE :keyword OR model LIKE :keyword OR maker_link LIKE :keyword OR model_link LIKE :keyword OR memo LIKE :keyword",
						SensorInfo.class);
				listQuery.setParameter("keyword", '%' + search + '%');
			} else {
				listQuery = session.createQuery("FROM SensorInfo", SensorInfo.class);
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			List<SensorInfo> result = listQuery.getResultList();

			Iterator<SensorInfo> it = result.iterator();
			while (it.hasNext()) {
				SensorInfo aRow = it.next();
				sensorInfoList.add(new SensorInfoVo(aRow, true));
			}

			LOGGER.info("5) Return SensorInfo !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
			listHelper.setResult(sensorInfoList);
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
	@Operation(summary = "SensorInfo API - Create")
	@POST
	@Path("/sensorInfo")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<SensorInfoVo> postSensorInfo(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @RequestBody SensorInfoVo sensorInfo) {
		CompletableFuture<SensorInfoVo> future = new CompletableFuture<>();
		Session session = null;
		Transaction transaction = null;
		try {
			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Begin Transaction !!!!!!!!!!!!!");
			transaction = session.beginTransaction();

			LOGGER.info("4) Save SensorInfo Entity !!!!!!!!!!!!!");
			SensorInfo aRow = new SensorInfo();
			aRow.setMaker(sensorInfo.getMaker());
			aRow.setModel(sensorInfo.getModel());
			aRow.setNetType(sensorInfo.getNetType());
			aRow.setDataType(sensorInfo.getDataType());
			aRow.setMakerLink(sensorInfo.getMakerLink());
			aRow.setModelLink(sensorInfo.getModelLink());
			aRow.setMemo(sensorInfo.getMemo());
			aRow.setFileList(Collections.emptySet());
			session.save(aRow);

			for (FileVo fv : sensorInfo.getFileList()) {
				FileBase fb = session.find(FileBase.class, fv.getId());
				SensorInfoFiles rRow = new SensorInfoFiles();
				rRow.setFileBase(fb);
				rRow.setSensorInfo(aRow);
				session.save(rRow);
			}

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return FileInfo !!!!!!!!!!!!!!!!!!!");
			future.complete(new SensorInfoVo(aRow));
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
	@Operation(summary = "SensorInfo API - Update")
	@PUT
	@Path("/sensorInfo/{sensorInfoId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<SensorInfoVo> putSensorInfo(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("sensorInfoId") Long sensorInfoId, @Parameter @RequestBody SensorInfoVo sensorInfo) {
		CompletableFuture<SensorInfoVo> future = new CompletableFuture<>();
		if (sensorInfoId == null || sensorInfo == null || sensorInfoId != sensorInfo.getId()) {
			request.response("Invalid SensorInfoId : " + sensorInfoId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Save User Entity !!!!!!!!!!!!!");
			SensorInfo aRow = session.find(SensorInfo.class, sensorInfoId);
			aRow.setMaker(sensorInfo.getMaker());
			aRow.setModel(sensorInfo.getModel());
			aRow.setNetType(sensorInfo.getNetType());
			aRow.setDataType(sensorInfo.getDataType());
			aRow.setMakerLink(sensorInfo.getMakerLink());
			aRow.setModelLink(sensorInfo.getModelLink());
			aRow.setMemo(sensorInfo.getMemo());
			Set<SensorInfoFiles> sifs = aRow.getFileList();
			for (FileVo fv : sensorInfo.getFileList()) {
				FileBase fb = session.find(FileBase.class, fv.getId());
				SensorInfoFiles rRow = new SensorInfoFiles();
				rRow.setFileBase(fb);
				rRow.setSensorInfo(aRow);
				session.save(rRow);
				sifs.add(rRow);
			}
			session.update(aRow);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return UserInfo !!!!!!!!!!!!!!!!!!!");
			future.complete(new SensorInfoVo(aRow));
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
	@Operation(summary = "SensorInfo API - Delete")
	@DELETE
	@Path("/sensorInfo/{sensorInfoId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<SensorInfoVo> deleteSensorInfo(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("sensorInfoId") Long sensorInfoId) {
		CompletableFuture<SensorInfoVo> future = new CompletableFuture<>();
		if (sensorInfoId == null || sensorInfoId <= 0) {
			request.response("Invalid SensorInfoId : " + sensorInfoId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Find and Delete SensorInfo Entity");
			List<String> delFiles = new ArrayList<String>();
			SensorInfo aRow = session.find(SensorInfo.class, sensorInfoId);
			SensorInfoVo retVo = new SensorInfoVo(aRow);
			for (SensorInfoFiles sif : aRow.getFileList()) {
				delFiles.add(sif.getFileBase().getId());
				session.delete(sif);
			}
			session.delete(aRow);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			FileDeleteHelper.deleteFiles(delFiles);

			LOGGER.info("6) Return SensorInfo !!!!!!!!!!!!!!!!!!!");
			future.complete(retVo);
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
	@Operation(summary = "SensorInfo API - Delete")
	@DELETE
	@Path("/sensorInfo/{sensorInfoId}/files")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<FileVo>> deleteSensorInfoFiles(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("sensorInfoId") Long sensorInfoId) {
		CompletableFuture<List<FileVo>> future = new CompletableFuture<>();
		if (sensorInfoId == null || sensorInfoId <= 0) {
			request.response("Invalid SensorInfoId : " + sensorInfoId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		List<String> fileIds = request.params().get("fileIds");
		if (fileIds == null || fileIds.size() == 0) {
			request.response("Invalid SensorInfoFileId : " + fileIds, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		List<FileVo> retVo = new ArrayList<FileVo>();

		Session session = null;
		Transaction transaction = null;
		try {
			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Begin Transaction !!!!!!!!!!!!!");
			transaction = session.beginTransaction();

			LOGGER.info("4) Find and Delete SensorInfoFiles Entity");
			List<String> delFiles = new ArrayList<String>();
			SensorInfo aRow = session.find(SensorInfo.class, sensorInfoId);
			for (SensorInfoFiles sif : aRow.getFileList()) {
				if (fileIds.contains(Long.toString(sif.getId()))) {
					retVo.add(new FileVo(sif.getFileBase(), sif.getId()));
					delFiles.add(sif.getFileBase().getId());
					session.delete(sif);
				}
			}

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			FileDeleteHelper.deleteFiles(delFiles);

			LOGGER.info("6) Return SensorInfoFiles !!!!!!!!!!!!!!!!!!!");
			future.complete(retVo);
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

}
