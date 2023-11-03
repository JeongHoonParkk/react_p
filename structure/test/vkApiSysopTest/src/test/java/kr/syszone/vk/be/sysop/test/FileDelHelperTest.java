package kr.syszone.vk.be.sysop.test;

import org.junit.Test;

import kr.syszone.vk.be.sysop.model.FileDeleteHelper;

public class FileDelHelperTest {

	public FileDelHelperTest() {

	}

	@Test
	public void testOp() {
		FileDeleteHelper.deleteFile("9999");
	}

	public static void main(String[] args) {
		FileDelHelperTest fdht = new FileDelHelperTest();
		fdht.testOp();
	}

}
