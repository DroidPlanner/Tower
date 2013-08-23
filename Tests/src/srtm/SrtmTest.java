package srtm;
import static org.junit.Assert.*;
import helpers.TimeLogger;

import org.junit.Test;

import com.srtm.Srtm;

public class SrtmTest {

	@Test
	public void testSrtm() {
		TimeLogger log = new TimeLogger();
		Srtm srtm = new Srtm("./bin/SRTM"); 

		System.out.println("Start");
		int alt = srtm.getData(-51.1439127, -29.7026708);
		log.tick();
		int sea = srtm.getData(-50.0360209, -29.8055343);
		log.tick();
		int high = srtm.getData(-50.0360209, -20.8055343);
		log.tick();
		System.out.println("Altitude:" + alt + " Sea:" + sea + " High:" + high);

		assertEquals(74, alt);
		assertEquals(1, sea);
		assertEquals(406, high);
	}

}
