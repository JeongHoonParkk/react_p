package kr.syszone.vk.be.sysop.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.Qna;
import kr.syszone.vk.be.db.entity.QnaFiles;

public class QnaVo {

	private Long id;
	private String title;
	private String memo;
	private String writerId;
	private Timestamp tsPost;
	private Timestamp tsEdit;
	private Integer isAnswer;
	private String answerMemo;
	private Timestamp tsAnswer;
	private String answerUserId;
	private List<FileVo> fileList;

	@JsonCreator
	public QnaVo() {

	}

	public QnaVo(Qna qa) {
		this(qa, true);
	}

	public QnaVo(Qna qa, Boolean syncFileList) {
		this.id = qa.getId();
		this.title = qa.getTitle();
		this.memo = qa.getMemo();
		this.writerId = qa.getWriterId();
		this.tsPost = qa.getTsPost();
		this.tsEdit = qa.getTsEdit();
		this.isAnswer = qa.getIsAnswer();
		this.answerMemo = qa.getAnswerMemo();
		this.tsAnswer = qa.getTsAnswer();
		this.answerUserId = qa.getAnswerUserId();
		this.isAnswer = qa.getIsAnswer();
		this.fileList = new ArrayList<FileVo>();

		if (syncFileList) {
			Set<QnaFiles> fbs = qa.getFileList();
			for (QnaFiles qf : fbs) {
				FileVo fv = new FileVo(qf.getFileBase(), qf.getId());
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

	public String getWriterId() {
		return writerId;
	}

	public void setWriterId(String writerId) {
		this.writerId = writerId;
	}

	public Timestamp getTsPost() {
		return tsPost;
	}

	public void setTsPost(Timestamp tsPost) {
		this.tsPost = tsPost;
	}

	public Timestamp getTsEdit() {
		return tsEdit;
	}

	public void setTsEdit(Timestamp tsEdit) {
		this.tsEdit = tsEdit;
	}

	public Integer getIsAnswer() {
		return isAnswer;
	}

	public void setIsAnswer(Integer isAnswer) {
		this.isAnswer = isAnswer;
	}

	public String getAnswerMemo() {
		return answerMemo;
	}

	public void setAnswerMemo(String answerMemo) {
		this.answerMemo = answerMemo;
	}

	public Timestamp getTsAnswer() {
		return tsAnswer;
	}

	public void setTsAnswer(Timestamp tsAnswer) {
		this.tsAnswer = tsAnswer;
	}

	public String getAnswerUserId() {
		return answerUserId;
	}

	public void setAnswerUserId(String answerUserId) {
		this.answerUserId = answerUserId;
	}

	public List<FileVo> getFileList() {
		return fileList;
	}

	public void setFileList(List<FileVo> fileList) {
		this.fileList = fileList;
	}

}
