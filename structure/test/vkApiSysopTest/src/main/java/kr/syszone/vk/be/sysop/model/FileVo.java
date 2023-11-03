package kr.syszone.vk.be.sysop.model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import kr.syszone.vk.be.db.entity.FileBase;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileVo {

	private String id;
	private String originalName;
	private String mimeType;
	private Integer fileSize;
	private Timestamp tsUpload;
	private Long linkedId;

	@JsonCreator
	public FileVo() {
	}

	public FileVo(FileBase fb, Long linkedId) {
		this.id = fb.getId();
		this.originalName = fb.getOriginalName();
		this.mimeType = fb.getMimeType();
		this.fileSize = fb.getFileSize();
		this.tsUpload = fb.getTsUpload();
		this.linkedId = linkedId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public Integer getFileSize() {
		return fileSize;
	}

	public void setFileSize(Integer fileSize) {
		this.fileSize = fileSize;
	}

	public Timestamp getTsUpload() {
		return tsUpload;
	}

	public void setTsUpload(Timestamp tsUpload) {
		this.tsUpload = tsUpload;
	}

	public Long getLinkedId() {
		return linkedId;
	}

	public void setLinkedId(Long linkedId) {
		this.linkedId = linkedId;
	}

}
