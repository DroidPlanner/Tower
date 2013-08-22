
public class Main {

	public static void main(String [ ] args){
		TimeLogger log = new TimeLogger();
		
		System.out.println("Start");
		
		int alt = Srtm.getData(-51.1439127,-29.7026708,"./bin/SRTM"); //Near my house
		log.tick();
		int sea = Srtm.getData(-50.0360209,-29.8055343,"./bin/SRTM"); //Sea level
		log.tick();
		int high = Srtm.getData(-50.0360209,-20.8055343,"./bin/SRTM"); //High place
		log.tick();
		
		System.out.println("Altitude:"+alt+" Sea:"+sea+ " High:"+high);
		}
}
