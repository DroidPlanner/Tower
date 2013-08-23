package srtm;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.srtm.Srtm;
import com.srtm.Srtm.OnProgressListner;

public class SrtmTest {
	private static final String PATH = "./bin/SRTM";
	Srtm srtm;
	protected int progressCalbacks;

	@Before
	public void Setup() {
		srtm = new Srtm(PATH);
	}

	@Test
	public void testSrtm() {
		int alt = srtm.getData(-51.1439127, -29.7026708);
		int sea = srtm.getData(-50.0360209, -29.8055343);
		int high = srtm.getData(-50.0360209, -20.8055343);

		assertEquals(74, alt);
		assertEquals(1, sea);
		assertEquals(406, high);
	}

	@Ignore("Since it needs to fetch a file this can be very slow")
	@Test
	public void testListner() {
		//	new File(PATH + "/S30W052.hgt").delete();
		
		srtm.setListner(new OnProgressListner() {
			@Override
			public void onProgress(String filename, int percentage) {
				System.out.println("OnProgress:" + filename + " %:"+ percentage);
			}
		});
		srtm.getData(-51.1439127, -29.7026708);		

	}

}
