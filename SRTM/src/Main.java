
public class Main {

	public static void main(String [ ] args){
		System.out.println("Start");
		
		SRTM.exists(0, 0,"./bin/SRTM");
		
		int alt = SRTM.getData(-51.1439127,-29.7026708,"./bin/SRTM"); //Near my house
		int sea = SRTM.getData(-50.0360209,-29.8055343,"./bin/SRTM"); //Sea level
		
		System.out.println("Altitude:"+alt+" Sea:"+sea);
		
		System.out.println("End");
	}
}
