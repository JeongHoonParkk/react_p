package kr.syszone.vk.be.sysop.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.Notice;
import kr.syszone.vk.be.db.entity.NoticeFiles;

public class NoticeVo {

	private Long id;
	private Integer noticeType;
	private String title;
	private String memo;
	private List<FileVo> fileList;


	@JsonCreator
	public NoticeVo() {

	}

	
	public NoticeVo(Notice nt) {
		this(nt, true);
	}

	public NoticeVo(Notice nt, Boolean syncFileList) {
		this.id = nt.getId();
		this.noticeType = nt.getNoticeType();
		this.title = nt.getTitle();
		this.memo = nt.getMemo();
		this.fileList = new ArrayList<FileVo>();

		if (syncFileList) {
			Set<NoticeFiles> fbs = nt.getFileList();
			for (NoticeFiles nf : fbs) {
				FileVo fv = new FileVo(nf.getFileBase(), nf.getId());
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


	public Integer getNoticeType() {
		return noticeType;
	}


	public void setNoticeType(Integer noticeType) {
		this.noticeType = noticeType;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
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
