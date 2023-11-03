package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.AirconInfo;

public class AirconVo {

	private String id;
	private Long aiId;
	private String memo;

	private String maker;
	private String model;

	@JsonCreator
	public AirconVo() {
	}

	public AirconVo(AirconInfo ai) {
		this(ai, true);
	}

	public AirconVo(AirconInfo ai, Boolean syncFileList) {
		this.aiId = ai.getId();
		this.maker = ai.getMaker();
		this.model = ai.getModel();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getAiId() {
		return aiId;
	}

	public void setAiId(Long aiId) {
		this.aiId = aiId;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getMaker() {
		return maker;
	}

	public void setMaker(String maker) {
		this.maker = maker;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

}
