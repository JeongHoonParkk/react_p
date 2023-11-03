package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.CompanyUser;

public class CompanyUserVo {

	private Long id;
	private String companyName;
	private String companyId;
	private Long groupId;
	private String groupName;
	private String userId;
	private String userName;
	private int userRole;
	private String memo;

	@JsonCreator
	public CompanyUserVo() {

	}

	// Company User List VO
	public CompanyUserVo(CompanyUser cu, String companyName, String cgName, String uName) {
		this.id = cu.getId();
		this.userRole = cu.getRole();
		this.memo = cu.getMemo();
		this.userId = cu.getUserId();
		this.companyId = cu.getCompanyId();
		this.companyName = companyName;
		this.userName = uName;
		this.groupId = cu.getGroupId();
		this.groupName = cgName;
	}

	public CompanyUserVo(CompanyUser cu) {
		this.id = cu.getId();
		this.userRole = cu.getRole();
		this.memo = cu.getMemo();
		this.userId = cu.getUserId();
		this.companyId = cu.getCompanyId();
		this.groupId = cu.getGroupId();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getUserRole() {
		return userRole;
	}

	public void setUserRole(int userRole) {
		this.userRole = userRole;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

}
