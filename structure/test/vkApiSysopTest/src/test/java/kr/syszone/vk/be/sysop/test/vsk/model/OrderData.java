package kr.syszone.vk.be.sysop.test.vsk.model;

import java.util.Arrays;

public class OrderData {

	private String scanCode; // orderNumber
	private String orderSheetId; // order Table id;
	private String plateNumber; // car number
	private String carNumber; // company vehicle name
	private String carGpsTracker; // GPS Tracker id
	private int tempMin; // orderNumber
	private int tempMax; // orderNumber
	private String[] iceboxIds; // package
	private int iceboxCount; // orderNumber
	private String[] deliverBlemacs; // SensorLog mac
	private String orderDate; // order
	private String orderState; // order
	private String deliverDate; //
	private String deliverStart;
	private String deliverEnd;
	private String packingEnd; // packaging End time
	private String tsCreate; // order tsCreate
	private String tsUpdate; // order tsUpdate

	public OrderData() {
		// TODO Auto-generated constructor stub
	}

	public String getScanCode() {
		return scanCode;
	}

	public void setScanCode(String scanCode) {
		this.scanCode = scanCode;
	}

	public String getOrderSheetId() {
		return orderSheetId;
	}

	public void setOrderSheetId(String orderSheetId) {
		this.orderSheetId = orderSheetId;
	}

	public String getPlateNumber() {
		return plateNumber;
	}

	public void setPlateNumber(String plateNumber) {
		this.plateNumber = plateNumber;
	}

	public String getCarNumber() {
		return carNumber;
	}

	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}

	public String getCarGpsTracker() {
		return carGpsTracker;
	}

	public void setCarGpsTracker(String carGpsTracker) {
		this.carGpsTracker = carGpsTracker;
	}

	public int getTempMin() {
		return tempMin;
	}

	public void setTempMin(int tempMin) {
		this.tempMin = tempMin;
	}

	public int getTempMax() {
		return tempMax;
	}

	public void setTempMax(int tempMax) {
		this.tempMax = tempMax;
	}

	public String[] getIceboxIds() {
		return iceboxIds;
	}

	public void setIceboxIds(String[] iceboxIds) {
		this.iceboxIds = iceboxIds;
	}

	public int getIceboxCount() {
		return iceboxCount;
	}

	public void setIceboxCount(int iceboxCount) {
		this.iceboxCount = iceboxCount;
	}

	public String[] getDeliverBlemacs() {
		return deliverBlemacs;
	}

	public void setDeliverBlemacs(String[] deliverBlemacs) {
		this.deliverBlemacs = deliverBlemacs;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public String getOrderState() {
		return orderState;
	}

	public void setOrderState(String orderState) {
		this.orderState = orderState;
	}

	public String getDeliverDate() {
		return deliverDate;
	}

	public void setDeliverDate(String deliverDate) {
		this.deliverDate = deliverDate;
	}

	public String getDeliverStart() {
		return deliverStart;
	}

	public void setDeliverStart(String deliverStart) {
		this.deliverStart = deliverStart;
	}

	public String getDeliverEnd() {
		return deliverEnd;
	}

	public void setDeliverEnd(String deliverEnd) {
		this.deliverEnd = deliverEnd;
	}

	public String getPackingEnd() {
		return packingEnd;
	}

	public void setPackingEnd(String packingEnd) {
		this.packingEnd = packingEnd;
	}

	public String getTsCreate() {
		return tsCreate;
	}

	public void setTsCreate(String tsCreate) {
		this.tsCreate = tsCreate;
	}

	public String getTsUpdate() {
		return tsUpdate;
	}

	public void setTsUpdate(String tsUpdate) {
		this.tsUpdate = tsUpdate;
	}

	@Override
	public String toString() {
		return "OrderData [scanCode=" + scanCode + ", orderSheetId=" + orderSheetId + ", plateNumber=" + plateNumber
				+ ", carNumber=" + carNumber + ", carGpsTracker=" + carGpsTracker + ", tempMin=" + tempMin
				+ ", tempMax=" + tempMax + ", iceboxIds=" + Arrays.toString(iceboxIds) + ", iceboxCount=" + iceboxCount
				+ ", deliverBlemacs=" + Arrays.toString(deliverBlemacs) + ", orderDate=" + orderDate + ", orderState="
				+ orderState + ", deliverDate=" + deliverDate + ", deliverStart=" + deliverStart + ", deliverEnd="
				+ deliverEnd + ", packingEnd=" + packingEnd + ", tsCreate=" + tsCreate + ", tsUpdate=" + tsUpdate + "]";
	}



}
