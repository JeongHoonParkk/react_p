package kr.syszone.vk.be.sysop.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.SensorInfo;
import kr.syszone.vk.be.db.entity.SensorInfoFiles;

public class SensorInfoVo {

	private Long id;
	private String maker;
	private String model;
	private Integer netType;
	private Integer dataType;
	private String makerLink;
	private String modelLink;
	private String memo;
	private List<FileVo> fileList;

	@JsonCreator
	public SensorInfoVo() {
	}

	public SensorInfoVo(SensorInfo si) {
		this(si, true);
	}

	public SensorInfoVo(SensorInfo si, Boolean syncFileList) {
		this.id = si.getId();
		this.maker = si.getMaker();
		this.model = si.getModel();
		this.netType = si.getNetType();
		this.dataType = si.getDataType();
		this.makerLink = si.getMakerLink();
		this.modelLink = si.getModelLink();
		this.memo = si.getMemo();
		this.fileList = new ArrayList<FileVo>();

		if (syncFileList) {
			Set<SensorInfoFiles> fbs = si.getFileList();
			for (SensorInfoFiles sif : fbs) {
				FileVo fv = new FileVo(sif.getFileBase(), sif.getId());
				fileList.add(fv);
			}
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getMakerLink() {
		return makerLink;
	}

	public void setMakerLink(String makerLink) {
		this.makerLink = makerLink;
	}

	public String getModelLink() {
		return modelLink;
	}

	public void setModelLink(String modelLink) {
		this.modelLink = modelLink;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public List<FileVo> getFileList() {
		return fileList;
	}

	public void setFileList(List<FileVo> fileList) {
		this.fileList = fileList;
	}

}
