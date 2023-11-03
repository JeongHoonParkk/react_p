package kr.syszone.vk.be.sysop.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ListHelper<T> {

	private String search;
	private String order;
	private String orderBy;

	private Long totalCount;
	private Integer rowsPerPage;
	private Integer requestPage;

	private List<T> result;

	@JsonCreator
	public ListHelper() {
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public Long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Long totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getRowsPerPage() {
		return rowsPerPage;
	}

	public void setRowsPerPage(Integer rowsPerPage) {
		this.rowsPerPage = rowsPerPage;
	}

	public Integer getRequestPage() {
		return requestPage;
	}

	public void setRequestPage(Integer requestPage) {
		this.requestPage = requestPage;
	}

	public List<T> getResult() {
		return result;
	}

	public void setResult(List<T> result) {
		this.result = result;
	}

}
