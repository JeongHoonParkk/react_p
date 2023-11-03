package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.Company;

public class TestVo {
	private String id;
	private String companyName;
	private String companyNo;
	private String ceo;
	private String postcode;
	private String addressPost;
	private String addressDetail;
	private String businessType;
	private String businessItem;
	private String phoneNo;
	private String homepage;
	private Double latitude;
	private Double longitude;

	@JsonCreator
	public TestVo() {

	}

	public TestVo(Company company) {
		this.id = company.getId();
		this.companyName = company.getCompanyName();
		this.companyNo = company.getCompanyNo();
		this.ceo = company.getCeo();
		this.postcode = company.getPostcode();
		this.addressPost = company.getAddressPost();
		this.addressDetail = company.getAddressDetail();
		this.businessType = company.getBusinessType();
		this.businessItem = company.getBusinessItem();
		this.phoneNo = company.getPhoneNo();
		this.homepage = company.getHomepage();
		this.latitude = company.getLatitude();
		this.longitude = company.getLongitude();
	}

	public Company getCompany() {

		Company company = new Company();

		company.setId(id);
		company.setCompanyName(companyName);
		company.setCompanyNo(companyNo);
		company.setCeo(ceo);
		company.setPostcode(postcode);
		company.setAddressPost(addressPost);
		company.setAddressDetail(addressDetail);
		company.setBusinessType(businessType);
		company.setBusinessItem(businessItem);
		company.setPhoneNo(phoneNo);
		company.setHomepage(homepage);
		company.setLatitude(latitude);
		company.setLongitude(longitude);

		return company;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCompanyNo() {
		return companyNo;
	}

	public void setCompanyNo(String companyNo) {
		this.companyNo = companyNo;
	}

	public String getCeo() {
		return ceo;
	}

	public void setCeo(String ceo) {
		this.ceo = ceo;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getAddressPost() {
		return addressPost;
	}

	public void setAddressPost(String addressPost) {
		this.addressPost = addressPost;
	}

	public String getAddressDetail() {
		return addressDetail;
	}

	public void setAddressDetail(String addressDetail) {
		this.addressDetail = addressDetail;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public String getBusinessItem() {
		return businessItem;
	}

	public void setBusinessItem(String businessItem) {
		this.businessItem = businessItem;
	}

	public String getphoneNo() {
		return phoneNo;
	}

	public void setphoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

}
