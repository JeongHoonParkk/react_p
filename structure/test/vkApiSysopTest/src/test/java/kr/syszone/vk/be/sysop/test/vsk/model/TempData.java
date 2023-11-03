package kr.syszone.vk.be.sysop.test.vsk.model;

public class TempData {

	private String ts;
	private int interval;
	private Double t1;
	private Double t2;

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public Double getT1() {
		return t1;
	}

	public void setT1(Double t1) {
		this.t1 = t1;
	}

	public Double getT2() {
		return t2;
	}

	public void setT2(Double t2) {
		this.t2 = t2;
	}

	@Override
	public String toString() {
		return "TempData [ts=" + ts + ", interval=" + interval + ", t1=" + t1 + ", t2=" + t2 + "]";
	}

}
