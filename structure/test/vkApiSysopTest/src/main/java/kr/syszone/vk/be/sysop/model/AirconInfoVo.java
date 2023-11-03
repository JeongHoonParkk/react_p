package kr.syszone.vk.be.sysop.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.AirconInfo;
import kr.syszone.vk.be.db.entity.AirconInfoFiles;

public class AirconInfoVo {

	private Long id;
	private String maker;
	private String model;
	private String makerLink;
	private String modelLink;
	private String memo;
	private List<FileVo> fileList;

	@JsonCreator
	public AirconInfoVo() {
	}

	public AirconInfoVo(AirconInfo ai) {
		this(ai, true);
	}

	public AirconInfoVo(AirconInfo ai, Boolean syncFileList) {
		this.id = ai.getId();
		this.maker = ai.getMaker();
		this.model = ai.getModel();
		this.makerLink = ai.getMakerLink();
		this.modelLink = ai.getModelLink();
		this.memo = ai.getMemo();
		this.fileList = new ArrayList<FileVo>();

		if (syncFileList) {
			Set<AirconInfoFiles> fbs = ai.getFileList();
			for (AirconInfoFiles sif : fbs) {
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
