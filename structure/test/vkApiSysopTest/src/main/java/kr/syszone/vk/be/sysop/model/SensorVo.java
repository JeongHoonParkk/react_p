package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.SensorInfo;

public class SensorVo {

	private String id;
	private Long siId;
	private String memo;

	private String maker;
	private String model;
	private Integer netType;
	private Integer dataType;

	@JsonCreator
	public SensorVo() {
	}

	public SensorVo(SensorInfo si) {
		this(si, true);
	}

	public SensorVo(SensorInfo si, Boolean syncFileList) {
		this.siId = si.getId();
		this.maker = si.getMaker();
		this.model = si.getModel();
		this.netType = si.getNetType();
		this.dataType = si.getDataType();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getSiId() {
		return siId;
	}

	public void setSiId(Long siId) {
		this.siId = siId;
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

	public Integer getNetType() {
		return netType;
	}

	public void setNetType(Integer netType) {
		this.netType = netType;
	}

	public Integer getDataType() {
		return dataType;
	}

	public void setDataType(Integer dataType) {
		this.dataType = dataType;
	}

}
