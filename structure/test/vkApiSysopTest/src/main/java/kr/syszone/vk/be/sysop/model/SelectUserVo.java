package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.User;

public class SelectUserVo {

	private String userId;
	private String userNm;

	@JsonCreator
	public SelectUserVo() {

	}
	
	

	public SelectUserVo(User aRow) {
		this.userId = aRow.getId();
		this.userNm = aRow.getName();
	}



	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserNm() {
		return userNm;
	}

	public void setUserNm(String userNm) {
		this.userNm = userNm;
	}

}
