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
import kr.syszone.vk.be.db.entity.Notice;
import kr.syszone.vk.be.db.entity.NoticeFiles;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.FileDeleteHelper;
import kr.syszone.vk.be.sysop.model.FileVo;
import kr.syszone.vk.be.sysop.model.ListHelper;
import kr.syszone.vk.be.sysop.model.NoticeVo;
import kr.syszone.vk.be.sysop.model.ProductApis;

@Path("/api/v1/sysop")
public class NoticeService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NoticeService.class);

	private ProductApis productApis;

	@JsonRequest
	@Operation(summary = "Notice API - Endpoint Information")
	@GET
	@Path("/notice")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ProductApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on PRODUCT!!");
		if (productApis == null) {
			productApis = new ProductApis();
		}
		CompletableFuture<ProductApis> future = new CompletableFuture<>();
		future.complete(productApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "Notice API - List")
	@GET
	@Path("/notice/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<NoticeVo>> getNoticeList(@Parameter @QueryParam("search") String search,
			@Parameter @QueryParam("order") String order, @Parameter @QueryParam("orderBy") String orderBy,
			@Parameter @QueryParam("page") Integer page, @Parameter @QueryParam("limit") Integer limit,
			@Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<NoticeVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/notice/list");

		ListHelper<NoticeVo> listHelper = new ListHelper<NoticeVo>();
		List<NoticeVo> noticeList = new ArrayList<NoticeVo>();
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

			LOGGER.info("3) Querying Notice Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery(
						"SELECT COUNT(id)" + " FROM Notice" + " WHERE title LIKE :keyword OR memo LIKE :keyword");
				countQuery.setParameter("keyword", '%' + search + '%');
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) FROM Notice");
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();
			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying Notice List !!!!!!!!!!!!!");
			Query<?> listQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery("FROM Notice" + " WHERE title LIKE :keyword OR memo LIKE :keyword");
				countQuery.setParameter("keyword", '%' + search + '%');

			} else {
				listQuery = session.createQuery("From Notice");
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			@SuppressWarnings("unchecked")
			List<Notice> result = (List<Notice>) listQuery.getResultList();

			Iterator<Notice> it = result.iterator();
			while (it.hasNext()) {
				Notice aNotice = it.next();
				noticeList.add(new NoticeVo(aNotice));
			}

			LOGGER.info("5) Return Notice List !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
			listHelper.setResult(noticeList);
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
	@Operation(summary = "Notice API - Create")
	@POST
	@Path("/notice")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<NoticeVo> productCreate(@Parameter(hidden = true) RakamHttpRequest request,
			@RequestBody NoticeVo body) {

		LOGGER.info("CREATE : /api/v1/notice/");
		CompletableFuture<NoticeVo> future = new CompletableFuture<>();

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
			Notice aRow = new Notice();
			aRow.setNoticeType(body.getNoticeType());
			aRow.setTitle(body.getTitle());
			aRow.setMemo(body.getMemo());
			aRow.setFileList(Collections.emptySet());

			session.save(aRow);

			for (FileVo fv : body.getFileList()) {
				FileBase fb = session.find(FileBase.class, fv.getId());
				NoticeFiles rRow = new NoticeFiles();
				rRow.setFileBase(fb);
				rRow.setNotice(aRow);
				session.save(rRow);
			}

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
	@Operation(summary = "Notice API - Update")
	@PUT
	@Path("/notice/{nId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<NoticeVo> productUpdate(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("nId") Long nId, @Parameter @RequestBody NoticeVo body) {

		LOGGER.info("UPDATE : /api/v1/sysop/notice/");

		CompletableFuture<NoticeVo> future = new CompletableFuture<>();
		if (nId == null || body == null || nId != body.getId()) {
			request.response("Invalid NoticeVo : " + nId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Update Notice Entity !!!!!!!!!!!!!");
			Notice aRow = session.find(Notice.class, nId);
			aRow.setNoticeType(body.getNoticeType());
			aRow.setTitle(body.getTitle());
			aRow.setMemo(body.getMemo());
			Set<NoticeFiles> nFs = aRow.getFileList();

			for (FileVo fv : body.getFileList()) {
				FileBase fb = session.find(FileBase.class, fv.getId());
				NoticeFiles rRow = new NoticeFiles();
				rRow.setFileBase(fb);
				rRow.setNotice(aRow);
				session.save(rRow);
				nFs.add(rRow);
			}

			session.update(aRow);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return NoticeVo !!!!!!!!!!!!!!!!!!!");
			future.complete(new NoticeVo(aRow));
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
	@Operation(summary = "Notice API - Delete")
	@DELETE
	@Path("/notice/{nId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<NoticeVo> productDelete(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("nId") Long nId) {

		CompletableFuture<NoticeVo> future = new CompletableFuture<>();

		if (nId == null || nId == 0) {
			request.response("Invalid Notice : " + nId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		LOGGER.info("DELETE : /api/v1/notice/");

		Session session = null;
		Transaction transaction = null;
		try {
			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Begin Transaction !!!!!!!!!!!!!");
			transaction = session.beginTransaction();

			LOGGER.info("4) Delete Notice !!!!!!!!!!!!!!!!!!!");

			Query<?> result = null;
			result = session.createQuery("FROM Notice" + " WHERE id = :keyword");
			result.setParameter("keyword", nId);

			List<String> delFiles = new ArrayList<String>();
			Notice aRow = session.find(Notice.class, nId);
			NoticeVo retVo = new NoticeVo(aRow);
			for (NoticeFiles sif : aRow.getFileList()) {
				delFiles.add(sif.getFileBase().getId());
				session.delete(sif);
			}
			session.delete(aRow);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			FileDeleteHelper.deleteFiles(delFiles);

			LOGGER.info("6) Return Notice !!!!!!!!!!!!!!!!!!!");
			future.complete(retVo);
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
	@Operation(summary = "Notice API - Delete")
	@DELETE
	@Path("/notice/{nId}/files")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<FileVo>> deleteNoticeFiles(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("nId") Long nId) {
		CompletableFuture<List<FileVo>> future = new CompletableFuture<>();
		if (nId == null || nId <= 0) {
			request.response("Invalid NoticeId : " + nId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		List<String> fileIds = request.params().get("fileIds");
		if (fileIds == null || fileIds.size() == 0) {
			request.response("Invalid NoticeFileId : " + fileIds, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Find and Delete NoticeFiles Entity");
			List<String> delFiles = new ArrayList<String>();
			Notice aRow = session.find(Notice.class, nId);
			for (NoticeFiles sif : aRow.getFileList()) {
				if (fileIds.contains(Long.toString(sif.getId()))) {
					retVo.add(new FileVo(sif.getFileBase(), sif.getId()));
					delFiles.add(sif.getFileBase().getId());
					session.delete(sif);
				}
			}

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();
			try {
				FileDeleteHelper.deleteFiles(delFiles);
			} catch (Exception e) {
				throw new Exception();
			}

			LOGGER.info("6) Return NoticeFiles !!!!!!!!!!!!!!!!!!!");
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
