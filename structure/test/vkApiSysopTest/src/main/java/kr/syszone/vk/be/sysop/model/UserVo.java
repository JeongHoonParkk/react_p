package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.User;

public class UserVo {

	private String id;
	private String password;
	private String name;
	private String phoneNo;
	private Integer state;

	@JsonCreator
	public UserVo() {
	}

	public UserVo(User user) {
		this.id = user.getId();
		this.password = user.getPassword();
		this.name = user.getName();
		this.state = user.getState();
		this.phoneNo = user.getPhoneNo();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

}
