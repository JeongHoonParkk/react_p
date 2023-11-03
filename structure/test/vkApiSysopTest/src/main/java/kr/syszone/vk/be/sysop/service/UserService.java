package kr.syszone.vk.be.sysop.service;

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
import kr.syszone.vk.be.db.entity.User;
import kr.syszone.vk.be.rest.HttpService;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.annotations.JsonRequest;
import kr.syszone.vk.be.sysop.model.ListHelper;
import kr.syszone.vk.be.sysop.model.SearchUserVo;
import kr.syszone.vk.be.sysop.model.SelectUserVo;
import kr.syszone.vk.be.sysop.model.UserApis;
import kr.syszone.vk.be.sysop.model.UserVo;

@Path("/api/v1/sysop")
public class UserService extends HttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	private UserApis userApiInfo;

	@JsonRequest
	@Operation(summary = "USER API - Endpoint Information")
	@GET
	@Path("/user")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<UserApis> getApiInfo(@Parameter(hidden = true) RakamHttpRequest request) {
		LOGGER.info("Request processing on USER!!");
		if (userApiInfo == null) {
			userApiInfo = new UserApis();
		}
		CompletableFuture<UserApis> future = new CompletableFuture<>();
		future.complete(userApiInfo);
		return future;
	}

	@JsonRequest
	@Operation(summary = "USER API - List")
	@GET
	@Path("/user/list")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<ListHelper<UserVo>> getUserList(@Parameter @QueryParam("search") String search,
			@Parameter @QueryParam("order") String order, @Parameter @QueryParam("orderBy") String orderBy,
			@Parameter @QueryParam("page") Integer page, @Parameter @QueryParam("limit") Integer limit,
			@Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<ListHelper<UserVo>> future = new CompletableFuture<>();
		ListHelper<UserVo> listHelper = new ListHelper<UserVo>();
		List<UserVo> userInfoList = new ArrayList<UserVo>();
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

			LOGGER.info("3) Querying User Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			if (search != null && search.length() > 0) {
				countQuery = session.createQuery(
						"SELECT COUNT(id) FROM User WHERE id LIKE :keyword OR name LIKE :keyword OR phone_no LIKE :keyword");
				countQuery.setParameter("keyword", search);
			} else {
				countQuery = session.createQuery("SELECT COUNT(id) FROM User");
			}
			Long rowsTotal = (Long) countQuery.uniqueResult();

			int offset = (listHelper.getRequestPage() - 1) * listHelper.getRowsPerPage();

			LOGGER.info("4) Querying User List !!!!!!!!!!!!!");
			Query<User> listQuery = null;
			if (search != null && search.length() > 0) {
				listQuery = session.createQuery(
						"FROM User WHERE id LIKE :keyword OR name LIKE :keyword OR phone_no LIKE :keyword", User.class);
				listQuery.setParameter("keyword", '%' + search + '%');
			} else {
				listQuery = session.createQuery("FROM User", User.class);
			}
			listQuery.setFirstResult(offset);
			listQuery.setMaxResults(listHelper.getRowsPerPage());

			List<User> result = listQuery.getResultList();

			Iterator<User> it = result.iterator();
			while (it.hasNext()) {
				User aRow = it.next();
				userInfoList.add(new UserVo(aRow));
			}

			LOGGER.info("5) Return UserInfo !!!!!!!!!!!!!!!!!!!");
			listHelper.setTotalCount(rowsTotal);
			listHelper.setResult(userInfoList);
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
	@Operation(summary = "USER API - Create")
	@POST
	@Path("/user")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<UserVo> postUser(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @RequestBody UserVo userInfo) {
		CompletableFuture<UserVo> future = new CompletableFuture<>();
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
			User aRow = new User();
			aRow.setId(userInfo.getId());
			aRow.setPassword(userInfo.getPassword());
			aRow.setName(userInfo.getName());
			aRow.setPhoneNo(userInfo.getPhoneNo());
			aRow.setState(userInfo.getState());
			session.save(aRow);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return FileInfo !!!!!!!!!!!!!!!!!!!");
			future.complete(new UserVo(aRow));
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
	@Operation(summary = "USER API - Update")
	@PUT
	@Path("/user/{userId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<UserVo> putUser(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("userId") String userId, @Parameter @RequestBody UserVo userInfo) {
		CompletableFuture<UserVo> future = new CompletableFuture<>();
		if (userId == null || userInfo == null || !userId.equals(userInfo.getId())) {
			request.response("Invalid UserId : " + userId, HttpResponseStatus.BAD_REQUEST).end();
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
			User aRow = new User();
			aRow.setId(userId);
			aRow.setPassword(userInfo.getPassword());
			aRow.setName(userInfo.getName());
			aRow.setPhoneNo(userInfo.getPhoneNo());
			aRow.setState(userInfo.getState());
			session.update(aRow);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return UserInfo !!!!!!!!!!!!!!!!!!!");
			future.complete(new UserVo(aRow));
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
	@Operation(summary = "USER API - Delete")
	@DELETE
	@Path("/user/{userId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<UserVo> deleteUser(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("userId") String userId) {
		CompletableFuture<UserVo> future = new CompletableFuture<>();
		if (userId == null || userId.length() == 0) {
			request.response("Invalid UserId : " + userId, HttpResponseStatus.BAD_REQUEST).end();
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

			LOGGER.info("4) Find and Delete User Entity !!!");
			User aRow = session.find(User.class, userId);
			session.delete(aRow);

			LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
			transaction.commit();

			LOGGER.info("6) Return UserInfo !!!!!!!!!!!!!!!!!!!");
			future.complete(new UserVo(aRow));
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
	@Operation(summary = "User API - User Search")
	@GET
	@Path("/users/{uId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<SearchUserVo>> getSearchUid(@Parameter(hidden = true) RakamHttpRequest request,
			@Parameter @PathParam("uId") String uId) {

		LOGGER.info("GET : /api/v1/user/{uId}");

		CompletableFuture<List<SearchUserVo>> future = new CompletableFuture<>();
		if (uId == null) {
			request.response("Invalid UserId : " + uId, HttpResponseStatus.BAD_REQUEST).end();
			return future;
		}

		List<SearchUserVo> userList = new ArrayList<SearchUserVo>();
		Session session = null;

		try {

			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Querying User Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			countQuery = session.createQuery("SELECT COUNT(id) FROM User");
			Long rowsTotal = (Long) countQuery.uniqueResult();
			if (rowsTotal == 0) {
				request.response("User is Empty : ", HttpResponseStatus.BAD_REQUEST).end();
				return future;
			}

			LOGGER.info("4) Querying User List !!!!!!!!!!!!!");
			Query<User> listQuery = null;
			listQuery = session.createQuery("FROM User WHERE id LIKE :uId", User.class).setParameter("uId",
					"%" + uId + "%");
			List<User> result = listQuery.getResultList();

			Iterator<User> it = result.iterator();
			while (it.hasNext()) {
				SearchUserVo vo = new SearchUserVo();
				User aUser = it.next();
				vo.setId(aUser.getId());
				vo.setName(aUser.getName());
				userList.add(vo);
			}

			LOGGER.info("5) Return Search USER List !!!!!!!!!!!!!!!!!!!");
			future.complete(userList);
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
	@Operation(summary = "USER SELECT API - List")
	@GET
	@Path("/users/select")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CompletableFuture<List<SelectUserVo>> getUserSelectList(
			@Parameter(hidden = true) RakamHttpRequest request) {
		CompletableFuture<List<SelectUserVo>> future = new CompletableFuture<>();
		List<SelectUserVo> userInfoList = new ArrayList<SelectUserVo>();
		Session session = null;
		try {

			LOGGER.info("1) Create Session Factory !!!!!!!!");
			SessionFactory sf = HibernateUtil.getSessionFactory();

			LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
			session = sf.openSession();

			LOGGER.info("3) Querying User SELECT Count !!!!!!!!!!!!!");
			Query<?> countQuery = null;
			countQuery = session.createQuery("SELECT COUNT(id) FROM User");
			Long rowsTotal = (Long) countQuery.uniqueResult();
			if (rowsTotal == 0) {
				request.response("User is Empty : ", HttpResponseStatus.BAD_REQUEST).end();
				return future;
			}

			LOGGER.info("4) Querying User SELECT List !!!!!!!!!!!!!");
			Query<User> listQuery = null;
			listQuery = session.createQuery("FROM User", User.class);

			List<User> result = listQuery.getResultList();

			Iterator<User> it = result.iterator();
			while (it.hasNext()) {
				User aRow = it.next();
				userInfoList.add(new SelectUserVo(aRow));
			}

			LOGGER.info("5) Return Select List UserInfo !!!!!!!!!!!!!!!!!!!");
			future.complete(userInfoList);
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
