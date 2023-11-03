package kr.syszone.vk.be.sysop.service;

import java.sql.Timestamp;
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
import kr.syszone.vk.be.db.entity.Qna;
import kr.syszone.vk.be.db.entity.QnaFiles;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.FileDeleteHelper;
import kr.syszone.vk.be.sysop.model.FileVo;
import kr.syszone.vk.be.sysop.model.ListHelper;
import kr.syszone.vk.be.sysop.model.QnaApis;
import kr.syszone.vk.be.sysop.model.QnaVo;

@Path("/api/v1/sysop")
public class QnaService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(QnaService.class);

	private QnaApis qnaApis;

	@JsonRequest
	@Operation(summary = "QnA API - Endpoint Information")
	@GET
	@Path("/qna")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<QnaApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on QnA!!");
		if (qnaApis == null) {
			qnaApis = new QnaApis();
		}
		CompletableFuture<QnaApis> future = new CompletableFuture<>();
		future.complete(qnaApis);
		return future;
	}

	@JsonRequest
	@Operation(summary = "QnA API - List")
	@GET
	@Path("/qna/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<QnaVo>> getQnaList(@Parameter @QueryParam("search") String search,
			@Parameter @QueryParam("order") String order, @Parameter @QueryParam("orderBy") String orderBy,
			@Parameter @QueryParam("page") Integer page, @Parameter @QueryParam("limit") Integer limit,
			@Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<QnaVo>> future = new CompletableFuture<>();

		LOGGER.info("GET : /api/v1/qna/list");

		ListHelper<QnaVo> listHelper = new ListHelper<QnaVo>();
		List<QnaVo> QnaList = new ArrayList<QnaVo>();
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

			LOGGER.info("3) Querying Qna Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery(
						"SELECT COUNT(id)" + " FROM Qna" + " WHERE title LIKE :keyword OR memo LIKE :keyword");
				countQuery.setParameter("keyword", '%' + search + '%');
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) FROM Qna");
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();
			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying Qna List !!!!!!!!!!!!!");
			Query<?> listQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery("FROM Qna" + " WHERE title LIKE :keyword OR memo LIKE :keyword");
				countQuery.setParameter("keyword", '%' + search + '%');

			} else {
				listQuery = session.createQuery("From Qna");
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			@SuppressWarnings("unchecked")
			List<Qna> result = (List<Qna>) listQuery.getResultList();

			Iterator<Qna> it = result.iterator();
			while (it.hasNext()) {
				Qna aQna = it.next();
				QnaList.add(new QnaVo(aQna));
			}

			LOGGER.info("5) Return Qna List !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
			listHelper.setResult(QnaList);
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
	@Operation(summary = "Qna API - Create")
	@POST
	@Path("/qna")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<QnaVo> productCreate(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @RequestBody QnaVo body) {

		LOGGER.info("CREATE : /api/v1/qna/");
		CompletableFuture<QnaVo> future = new CompletableFuture<>();

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
			Qna aRow = new Qna();
			aRow.setTitle(body.getTitle());
			aRow.setMemo(body.getMemo());
			aRow.setWriterId(body.getWriterId());
			aRow.setTsPost(new Timestamp(System.currentTimeMillis()));
			aRow.setFileList(Collections.emptySet());
			aRow.setIsAnswer(0);

			session.save(aRow);

			for (FileVo fv : body.getFileList()) {
				FileBase fb = session.find(FileBase.class, fv.getId());
				QnaFiles rRow = new QnaFiles();
				rRow.setFileBase(fb);
				rRow.setQna(aRow);
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
	@Operation(summary = "Qna API - Update")
	@PUT
	@Path("/qna/{qId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<QnaVo> qnaUpdate(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("qId") Long qId, @Parameter @RequestBody QnaVo body) {

		LOGGER.info("UPDATE : /api/v1/sysop/qna/");

		CompletableFuture<QnaVo> future = new CompletableFuture<>();
		if (qId == null || body == null || qId != body.getId()) {
			request.response("Invalid QnaVo : " + qId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Update Qna Entity !!!!!!!!!!!!!");
			Qna aRow = session.find(Qna.class, qId);
			aRow.setTitle(body.getTitle());
			aRow.setMemo(body.getMemo());
			aRow.setTsEdit(new Timestamp(System.currentTimeMillis()));
			Set<QnaFiles> qFs = aRow.getFileList();

			for (FileVo fv : body.getFileList()) {
				FileBase fb = session.find(FileBase.class, fv.getId());
				LOGGER.info("FileBase : {}", fb.getOriginalName());
				QnaFiles rRow = new QnaFiles();
				rRow.setFileBase(fb);
				rRow.setQna(aRow);
				session.save(rRow);
				qFs.add(rRow);
			}

			session.update(aRow);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return QnaVo !!!!!!!!!!!!!!!!!!!");
			future.complete(new QnaVo(aRow));
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
	@Operation(summary = "Qna Answer API - Update")
	@PUT
	@Path("/qna/answer/{qId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<QnaVo> answerUpdate(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("qId") Long qId, @Parameter @RequestBody QnaVo body) {

		LOGGER.info("UPDATE : /api/v1/sysop/qna/answer/");

		CompletableFuture<QnaVo> future = new CompletableFuture<>();
		if (qId == null || body == null || qId != body.getId()) {
			request.response("Invalid QnaVo : " + qId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Update Qna Entity !!!!!!!!!!!!!");
			Qna aRow = session.find(Qna.class, qId);
			aRow.setIsAnswer(1);
			aRow.setAnswerMemo(body.getAnswerMemo());
			aRow.setTsAnswer(new Timestamp(System.currentTimeMillis()));
			aRow.setAnswerUserId(body.getAnswerUserId());
			session.update(aRow);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return QnaVo !!!!!!!!!!!!!!!!!!!");
			future.complete(new QnaVo(aRow));
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
	@Operation(summary = "Qna API - Delete")
	@DELETE
	@Path("/qna/{qId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<QnaVo> productDelete(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("qId") Long qId) {

		CompletableFuture<QnaVo> future = new CompletableFuture<>();

		if (qId == null || qId == 0) {
			request.response("Invalid Qna : " + qId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		LOGGER.info("DELETE : /api/v1/qna/");

		Session session = null;
		Transaction transaction = null;
		try {
			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Begin Transaction !!!!!!!!!!!!!");
			transaction = session.beginTransaction();

			LOGGER.info("4) Delete Qna !!!!!!!!!!!!!!!!!!!");

			Query<?> result = null;
			result = session.createQuery("FROM Qna" + " WHERE id = :keyword");
			result.setParameter("keyword", qId);

			List<String> delFiles = new ArrayList<String>();
			Qna aRow = session.find(Qna.class, qId);
			QnaVo retVo = new QnaVo(aRow);
			for (QnaFiles sif : aRow.getFileList()) {
				delFiles.add(sif.getFileBase().getId());
				session.delete(sif);
			}
			session.delete(aRow);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			FileDeleteHelper.deleteFiles(delFiles);

			LOGGER.info("6) Return Qna !!!!!!!!!!!!!!!!!!!");
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
	@Operation(summary = "Qna API - Delete")
	@DELETE
	@Path("/qna/{qId}/files")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<FileVo>> deleteQnaFiles(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("qId") Long qId) {
		CompletableFuture<List<FileVo>> future = new CompletableFuture<>();
		if (qId == null || qId <= 0) {
			request.response("Invalid QnaId : " + qId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		List<String> fileIds = request.params().get("fileIds");
		if (fileIds == null || fileIds.size() == 0) {
			request.response("Invalid QnaFileId : " + fileIds, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Find and Delete QnaFiles Entity");
			List<String> delFiles = new ArrayList<String>();
			Qna aRow = session.find(Qna.class, qId);
			for (QnaFiles sif : aRow.getFileList()) {
				if (fileIds.contains(Long.toString(sif.getId()))) {
					retVo.add(new FileVo(sif.getFileBase(), sif.getId()));
					delFiles.add(sif.getFileBase().getId());
					session.delete(sif);
				}
			}

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			FileDeleteHelper.deleteFiles(delFiles);

			LOGGER.info("6) Return QnaFiles !!!!!!!!!!!!!!!!!!!");
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
