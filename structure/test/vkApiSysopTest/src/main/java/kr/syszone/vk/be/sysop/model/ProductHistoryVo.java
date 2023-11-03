package kr.syszone.vk.be.sysop.model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.ProductHistory;

public class ProductHistoryVo {

	private Long id;
    private Long productId;
    private String productName;
    private int workType;
    private String name;
    private String prevCompanyId;
    private String prevCompanyName;
    private String nextCompanyId;
    private String nextCompanyName;
    private Timestamp tsWorkStart;
    private Timestamp tsWorkEnd;
    private String memo;
    
    

	@JsonCreator
	public ProductHistoryVo() {

	}
	
	

	public ProductHistoryVo(ProductHistory aPh, String preCName, String nextCName, String pName) {
		this.id = aPh.getId();
		this.productId = aPh.getProductId();
		this.productName = pName;
		this.workType = aPh.getWorkType();
		this.name = aPh.getName();
		this.prevCompanyId = aPh.getPrevCompanyId();
		this.prevCompanyName = preCName;
		this.nextCompanyId = aPh.getNextCompanyId();
		this.nextCompanyName = nextCName;
		this.tsWorkStart = aPh.getTsWorkStart();
		this.tsWorkEnd = aPh.getTsWorkEnd();
		this.memo = aPh.getMemo();
	}


	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public Long getProductId() {
		return productId;
	}



	public void setProductId(Long productId) {
		this.productId = productId;
	}



	public String getProductName() {
		return productName;
	}



	public void setProductName(String productName) {
		this.productName = productName;
	}



	public int getWorkType() {
		return workType;
	}



	public void setWorkType(int workType) {
		this.workType = workType;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getPrevCompanyId() {
		return prevCompanyId;
	}



	public void setPrevCompanyId(String prevCompanyId) {
		this.prevCompanyId = prevCompanyId;
	}



	public String getPrevCompanyName() {
		return prevCompanyName;
	}



	public void setPrevCompanyName(String prevCompanyName) {
		this.prevCompanyName = prevCompanyName;
	}



	public String getNextCompanyId() {
		return nextCompanyId;
	}



	public void setNextCompanyId(String nextCompanyId) {
		this.nextCompanyId = nextCompanyId;
	}



	public String getnextCompanyName() {
		return nextCompanyName;
	}



	public void setnextCompanyName(String nextCompanyName) {
		this.nextCompanyName = nextCompanyName;
	}



	public Timestamp getTsWorkStart() {
		return tsWorkStart;
	}



	public void setTsWorkStart(Timestamp tsWorkStart) {
		this.tsWorkStart = tsWorkStart;
	}



	public Timestamp getTsWorkEnd() {
		return tsWorkEnd;
	}



	public void setTsWorkEnd(Timestamp tsWorkEnd) {
		this.tsWorkEnd = tsWorkEnd;
	}



	public String getMemo() {
		return memo;
	}



	public void setMemo(String memo) {
		this.memo = memo;
	}
	
	
}
