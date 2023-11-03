package kr.syszone.vk.be.sysop.test.vsk;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import kr.syszone.vk.be.db.HibernateUtil;
import kr.syszone.vk.be.db.entity.Order;
import kr.syszone.vk.be.db.entity.OrderDelivery;
import kr.syszone.vk.be.db.entity.OrderDetail;
import kr.syszone.vk.be.db.entity.Product;
import kr.syszone.vk.be.db.entity.ProductHistory;
import kr.syszone.vk.be.db.entity.Sensor;
import kr.syszone.vk.be.db.entity.SensorAssign;
import kr.syszone.vk.be.db.entity.Vehicle;
import kr.syszone.vk.be.db.entity.data.GnssLog;
import kr.syszone.vk.be.db.entity.data.SensorLog;
import kr.syszone.vk.be.rest.RakamHttpRequest;
import kr.syszone.vk.be.rest.Response;
import kr.syszone.vk.be.sysop.model.OrderVo;
import kr.syszone.vk.be.sysop.model.SensorAssignVo;
import kr.syszone.vk.be.sysop.test.vsk.model.GnssData;
import kr.syszone.vk.be.sysop.test.vsk.model.OrderData;
import kr.syszone.vk.be.sysop.test.vsk.model.TempData;

public class VskToVk {
	private static final Logger LOGGER = LoggerFactory.getLogger(VskToVk.class);

	private static final String URL = "http://59.9.223.3:30086/";
	private static final char[] TOKEN = "jl6ib0cUrDD8bKmf0tXsNEQ1LVMOZhJTjbo1H9wykmD2_qPJMzwoSDJ_MdSvnosUiKT8q8w3kI7j_1hdqeVdmQ=="
			.toCharArray();
	private static final String ORG = "syszone";
	private static final String BUCKET = "valuekeeper";

	private InfluxDBClient influxDBClient;
	private WriteApi writeApi;
	private static Long startTime;

	private int rowsOk = 0;
	private int rowsError = 0;

	public void initInflux() {
		influxDBClient = InfluxDBClientFactory.create(URL, TOKEN, ORG, BUCKET);
		writeApi = influxDBClient.makeWriteApi();
	}

	public void cleanup(Long startTime) {
		if (writeApi != null) {
			writeApi.close();
			writeApi = null;
		}
		if (influxDBClient != null) {
			influxDBClient.close();
			influxDBClient = null;
		}

		long stopTime = System.currentTimeMillis();

		long runTime = stopTime - startTime;
		int msec = (int) (runTime % 1000);
		long sec = ((runTime / 1000) % 60);
		long minute = ((runTime / 1000) / 60);

		System.out.println("Success : " + rowsOk + " Completed!! / Error : " + rowsError + " / RunTime : " + minute
				+ "m " + sec + "s" + msec + " msec");
	}

	public void influxGnssLog(List<GnssData> gnssList, String mac, String productId, String orderId) {
		startTime = System.currentTimeMillis();
		try {
			for (GnssData gnssData : gnssList) {

				Date tDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(gnssData.getTs_create());
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(tDate.getTime());
//				cal.add(Calendar.HOUR, -9);
//				System.out.println(cal.getTimeInMillis() + " | " + Instant.ofEpochMilli(cal.getTimeInMillis()));
				GnssLog gl = new GnssLog();
				gl.setTime(Instant.ofEpochMilli(cal.getTimeInMillis()));
				gl.setSid(mac);
				gl.setAccuracy(gnssData.getGpsAccuracy());
				gl.setAngle(gnssData.getGpsBearing());
				gl.setLatitude(gnssData.getGpsLat());
				gl.setLongitude(gnssData.getGpsLng());
				gl.setSpeed(gnssData.getGpsSpeed());
				gl.setContext(productId);
				gl.setDeliveryCode(orderId);

				writeApi.writeMeasurement(WritePrecision.MS, gl);
				rowsOk++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cleanup(startTime);
		}
	}

	public void influxSensorLog(List<TempData> tempList, String mac, String productId, String orderId) {
		startTime = System.currentTimeMillis();
		try {
			for (TempData tempData : tempList) {

				Date tDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(tempData.getTs());
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(tDate.getTime());
//				cal.add(Calendar.HOUR, -9);
//				System.out.println(cal.getTimeInMillis() + " | " + Instant.ofEpochMilli(cal.getTimeInMillis()));
				SensorLog sl = new SensorLog();
				sl.setTime(Instant.ofEpochMilli(cal.getTimeInMillis()));
				sl.setSid(mac);
				sl.setTemperature(tempData.getT1());
				sl.setContext(productId);
				sl.setDeliveryCode(orderId);

				writeApi.writeMeasurement(WritePrecision.MS, sl);
				rowsOk++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cleanup(startTime);
		}

	}

	public void orderSave(List<OrderData> list) {

		LOGGER.info("CREATE : Vsk To Valuekeeper!!");

		Session session = null;
		Transaction transaction = null;

		for (OrderData oData : list) {
			try {
				LOGGER.info("1) Create Session Factory !!!!!!!!");
				SessionFactory sf = HibernateUtil.getSessionFactory();

				LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
				session = sf.openSession();
				LOGGER.info("3) Begin Transaction !!!!!!!!!!!!!");
				transaction = session.beginTransaction();

				LOGGER.info("4) Querying SAVE !!!!!!!!!!!!!!!");

				Date ocDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(oData.getTsCreate());

				Vehicle vehicle = session.createQuery("FROM Vehicle WHERE carNo = :number", Vehicle.class)
						.setParameter("number", oData.getPlateNumber()).getSingleResult();

				Order saveO = new Order();
				saveO.setName(oData.getScanCode());
				saveO.setOrderType(0);
				saveO.setOrderCompanyId("gccell");
				saveO.setExecuteCompanyId("gccell");
				saveO.setState(0);
				saveO.setTsRegister(new Timestamp(ocDate.getTime()));
				Date dsDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(oData.getDeliverStart());
				saveO.setTsStart(new Timestamp(dsDate.getTime()));
				Date deDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(oData.getDeliverEnd());
				saveO.setTsEnd(new Timestamp(deDate.getTime()));
				session.save(saveO);

				for (String sensorMac : oData.getDeliverBlemacs()) {
					Product p = new Product();
					p.setOwnerId("gccell");
					p.setManagerId("gccell");
					p.setState(0);
					p.setAmount(1);
					p.setTempLow(Double.valueOf(oData.getTempMin()));
					p.setTempHigh(Double.valueOf(oData.getTempMax()));
					p.setTorSec(0);
					p.setTsRegister(new Timestamp(ocDate.getTime()));
					SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd");
					p.setName("국건영 : " + sdf.format(dsDate) + " ("
							+ vehicle.getCarNo().substring(vehicle.getCarNo().length() - 4, vehicle.getCarNo().length())
							+ ")");
					session.save(p);

					// Save to influx
					VskGetDataTest vdt = new VskGetDataTest();
					vdt.getGnssData(oData.getCarGpsTracker(), oData.getDeliverStart(), oData.getDeliverEnd(),
							p.getId().toString(), saveO.getId().toString()); // GpsMac,
					vdt.getTempData(sensorMac, oData.getDeliverStart(), oData.getDeliverEnd(), p.getId().toString(),
							saveO.getId().toString()); // TempSensor,

					OrderDetail od = new OrderDetail();
					od.setOrderId(saveO.getId());
					od.setProductId(p.getId());
					od.setVehicleId(vehicle.getId());
					od.setSensorId(oData.getCarGpsTracker());
					session.save(od);

					ProductHistory ph = new ProductHistory();
					ph.setProductId(p.getId());
					ph.setWorkType(0);
					ph.setName("최초등록");
					ph.setPrevCompanyId("gccell");
					ph.setNextCompanyId("gccell");
					ph.setTsWorkStart(new Timestamp(ocDate.getTime()));
					ph.setTsWorkEnd(new Timestamp(ocDate.getTime()));
					session.save(ph);

					if (oData.getPackingEnd() != null) {
						ProductHistory ph2 = new ProductHistory();
						ph2.setProductId(p.getId());
						ph2.setWorkType(4);
						ph2.setName("포장");
						ph2.setPrevCompanyId("gccell");
						ph2.setNextCompanyId("gccell");
						Date peDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(oData.getPackingEnd());
						ph2.setTsWorkEnd(new Timestamp(peDate.getTime()));
						session.save(ph2);
					}

					OrderDetail newOD = new OrderDetail();
					newOD.setOrderId(saveO.getId());
					newOD.setProductId(p.getId());
					newOD.setVehicleId(vehicle.getId());
					newOD.setSensorId(sensorMac);
					session.save(newOD);
				}

				LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
				transaction.commit();

			} catch (Exception e) {
				LOGGER.error("6) Operation Failed !!!!!!!!!!!!!", e);
				if (transaction != null) {
					transaction.rollback();
				}
			}
		}

		LOGGER.info("6) Close Session !!!!!!!!!!!!!!!!!");
		if (session != null) {
			session.close();
		}

	}

	public void orderUpdate(List<OrderData> list) {

		LOGGER.info("CREATE : Vsk To Valuekeeper!!");

		Session session = null;
		Transaction transaction = null;

		for (OrderData oData : list) {
			try {
				LOGGER.info("1) Create Session Factory !!!!!!!!");
				SessionFactory sf = HibernateUtil.getSessionFactory();

				LOGGER.info("2) Open Session !!!!!!!!!!!!!!!!!!");
				session = sf.openSession();
				LOGGER.info("3) Begin Transaction !!!!!!!!!!!!!");
				transaction = session.beginTransaction();

				LOGGER.info("4) Querying SAVE !!!!!!!!!!!!!!!");
				LOGGER.info("4-1) Operating : "+ oData.getScanCode());
				Date ocDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(oData.getTsCreate());

				Order order = session.createQuery("FROM Order WHERE name = :oName", Order.class)
						.setParameter("oName", oData.getScanCode()).uniqueResult();
				order.setTsRegister(new Timestamp(ocDate.getTime()));
				session.update(order);

				LOGGER.info("5) Commit Transaction !!!!!!!!!!!!");
				transaction.commit();

			} catch (Exception e) {
				LOGGER.error("6) Operation Failed !!!!!!!!!!!!!", e);
				if (transaction != null) {
					transaction.rollback();
				}
			}
		}

		LOGGER.info("6) Close Session !!!!!!!!!!!!!!!!!");
		if (session != null) {
			session.close();
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String num = "경기12하1234";
		System.out.println(num.substring(num.length() - 4, num.length()));
	}

}
