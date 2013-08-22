
public class Main {

	static long time;
	public static void main(String [ ] args){
		System.out.println("Start");
		time = System.currentTimeMillis();
		
		int alt = SRTM.getData(-51.1439127,-29.7026708,"./bin/SRTM"); //Near my house
		System.out.println("Time:"+(System.currentTimeMillis()-time)+"ms");
		time = System.currentTimeMillis();
		int sea = SRTM.getData(-50.0360209,-20.8055343,"./bin/SRTM"); //Sea level
		System.out.println("Time:"+(System.currentTimeMillis()-time)+"ms");
		time = System.currentTimeMillis();
		sea = SRTM.getData(-50.0360209,-10.8055343,"./bin/SRTM"); //Sea level
		System.out.println("Time:"+(System.currentTimeMillis()-time)+"ms");
		time = System.currentTimeMillis();
		
		System.out.println("Altitude:"+alt+" Sea:"+sea);
		}
}
