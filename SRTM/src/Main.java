
public class Main {

	public static void main(String [ ] args){
		System.out.println("Start");
		
		SRTM.exists(0, 0,"./bin/SRTM");
		
		SRTM srtm = SRTM.get(-51, -29,"./bin/SRTM");
		
		System.out.println("End");
	}
}
