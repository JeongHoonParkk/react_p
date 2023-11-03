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
import kr.syszone.vk.be.db.entity.AirconInfo;
import kr.syszone.vk.be.db.entity.AirconInfoFiles;
import kr.syszone.vk.be.db.entity.FileBase;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.AirconInfoApis;
import kr.syszone.vk.be.sysop.model.AirconInfoVo;
import kr.syszone.vk.be.sysop.model.FileDeleteHelper;
import kr.syszone.vk.be.sysop.model.FileVo;
import kr.syszone.vk.be.sysop.model.ListHelper;

@Path("/api/v1/sysop")
public class AirconInfoService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AirconInfoService.class);

	private AirconInfoApis AirconInfoApis;

	@JsonRequest
	@Operation(summary = "AirconInfo API - Endpoint Information")
	@GET
	@Path("/airconInfo")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<AirconInfoApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on USER!!");
		if (AirconInfoApis == null) {
			AirconInfoApis = new AirconInfoApis();
		}
		CompletableFuture<AirconInfoApis> future = new CompletableFuture<>();
		future.complete(AirconInfoApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "AirconInfo API - List")
	@GET
	@Path("/airconInfo/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<AirconInfoVo>> getAirconInfoList(@Parameter @QueryParam("search") String search,
			@Parameter @QueryParam("order") String order, @Parameter @QueryParam("orderBy") String orderBy,
			@Parameter @QueryParam("page") Integer page, @Parameter @QueryParam("limit") Integer limit,
			@Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<AirconInfoVo>> future = new CompletableFuture<>();
		ListHelper<AirconInfoVo> listHelper = new ListHelper<AirconInfoVo>();
		List<AirconInfoVo> AirconInfoList = new ArrayList<AirconInfoVo>();
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

			LOGGER.info("3) Querying AirconInfo Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery(
						"SELECT COUNT(id) FROM AirconInfo WHERE maker LIKE :keyword OR model LIKE :keyword OR maker_link LIKE :keyword OR model_link LIKE :keyword OR memo LIKE :keyword");
				countQuery.setParameter("keyword", '%' + search + '%');
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) FROM AirconInfo");
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();

			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying AirconInfo List !!!!!!!!!!!!!");
			Query<AirconInfo> listQuery = null;
			if (search != null && search.length() > 0) {
				listQuery = session.createQuery(
						"FROM AirconInfo WHERE maker LIKE :keyword OR model LIKE :keyword OR maker_link LIKE :keyword OR model_link LIKE :keyword OR memo LIKE :keyword",
						AirconInfo.class);
				listQuery.setParameter("keyword", '%' + search + '%');
			} else {
				listQuery = session.createQuery("FROM AirconInfo", AirconInfo.class);
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			List<AirconInfo> result = listQuery.getResultList();

			Iterator<AirconInfo> it = result.iterator();
			while (it.hasNext()) {
				AirconInfo aRow = it.next();
				AirconInfoList.add(new AirconInfoVo(aRow, true));
			}

			LOGGER.info("5) Return AirconInfo !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
			listHelper.setResult(AirconInfoList);
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
	@Operation(summary = "AirconInfo API - Create")
	@POST
	@Path("/airconInfo")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<AirconInfoVo> postAirconInfo(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @RequestBody AirconInfoVo airconInfo) {
		CompletableFuture<AirconInfoVo> future = new CompletableFuture<>();
		Session session = null;
		Transaction transaction = null;
		try {
			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Begin Transaction !!!!!!!!!!!!!");
			transaction = session.beginTransaction();

			LOGGER.info("4) Save AirconInfo Entity !!!!!!!!!!!!!");
			AirconInfo aRow = new AirconInfo();
			aRow.setMaker(airconInfo.getMaker());
			aRow.setModel(airconInfo.getModel());
			aRow.setMakerLink(airconInfo.getMakerLink());
			aRow.setModelLink(airconInfo.getModelLink());
			aRow.setMemo(airconInfo.getMemo());
			aRow.setFileList(Collections.emptySet());
			session.save(aRow);

			for (FileVo fv : airconInfo.getFileList()) {
				FileBase fb = session.find(FileBase.class, fv.getId());
				AirconInfoFiles rRow = new AirconInfoFiles();
				rRow.setFileBase(fb);
				rRow.setAirconInfo(aRow);
				session.save(rRow);
			}

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return FileInfo !!!!!!!!!!!!!!!!!!!");
			future.complete(new AirconInfoVo(aRow));
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
	@Operation(summary = "AirconInfo API - Update")
	@PUT
	@Path("/airconInfo/{airconInfoId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<AirconInfoVo> putAirconInfo(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("airconInfoId") Long airconInfoId, @Parameter @RequestBody AirconInfoVo airconInfo) {
		CompletableFuture<AirconInfoVo> future = new CompletableFuture<>();
		if (airconInfoId == null || airconInfo == null || airconInfoId != airconInfo.getId()) {
			request.response("Invalid airconInfoId : " + airconInfoId, HttpResponseStatus.BAD_REQUEST).end();
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
			AirconInfo aRow = session.find(AirconInfo.class, airconInfoId);
			aRow.setMaker(airconInfo.getMaker());
			aRow.setModel(airconInfo.getModel());
			aRow.setMakerLink(airconInfo.getMakerLink());
			aRow.setModelLink(airconInfo.getModelLink());
			aRow.setMemo(airconInfo.getMemo());
			Set<AirconInfoFiles> sifs = aRow.getFileList();
			for (FileVo fv : airconInfo.getFileList()) {
				FileBase fb = session.find(FileBase.class, fv.getId());
				AirconInfoFiles rRow = new AirconInfoFiles();
				rRow.setFileBase(fb);
				rRow.setAirconInfo(aRow);
				session.save(rRow);
				sifs.add(rRow);
			}
			session.update(aRow);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return UserInfo !!!!!!!!!!!!!!!!!!!");
			future.complete(new AirconInfoVo(aRow));
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
	@Operation(summary = "AirconInfo API - Delete")
	@DELETE
	@Path("/airconInfo/{airconInfoId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<AirconInfoVo> deleteAirconInfo(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("airconInfoId") Long airconInfoId) {
		CompletableFuture<AirconInfoVo> future = new CompletableFuture<>();
		if (airconInfoId == null || airconInfoId <= 0) {
			request.response("Invalid AirconInfoId : " + airconInfoId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Find and Delete AirconInfo Entity");
			List<String> delFiles = new ArrayList<String>();
			AirconInfo aRow = session.find(AirconInfo.class, airconInfoId);
			AirconInfoVo retVo = new AirconInfoVo(aRow);
			for (AirconInfoFiles sif : aRow.getFileList()) {
				delFiles.add(sif.getFileBase().getId());
				session.delete(sif);
			}
			session.delete(aRow);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			FileDeleteHelper.deleteFiles(delFiles);

			LOGGER.info("6) Return AirconInfo !!!!!!!!!!!!!!!!!!!");
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
	@Operation(summary = "AirconInfo API - Delete")
	@DELETE
	@Path("/airconInfo/{airconInfoId}/files")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<FileVo>> deleteAirconInfoFiles(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("airconInfoId") Long airconInfoId) {
		CompletableFuture<List<FileVo>> future = new CompletableFuture<>();
		if (airconInfoId == null || airconInfoId <= 0) {
			request.response("Invalid AirconInfoId : " + airconInfoId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		List<String> fileIds = request.params().get("fileIds");
		if (fileIds == null || fileIds.size() == 0) {
			request.response("Invalid AirconInfoFileId : " + fileIds, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Find and Delete AirconInfoFiles Entity");
			List<String> delFiles = new ArrayList<String>();
			AirconInfo aRow = session.find(AirconInfo.class, airconInfoId);
			for (AirconInfoFiles sif : aRow.getFileList()) {
				if (fileIds.contains(Long.toString(sif.getId()))) {
					retVo.add(new FileVo(sif.getFileBase(), sif.getId()));
					delFiles.add(sif.getFileBase().getId());
					session.delete(sif);
				}
			}

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			FileDeleteHelper.deleteFiles(delFiles);

			LOGGER.info("6) Return AirconInfoFiles !!!!!!!!!!!!!!!!!!!");
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
