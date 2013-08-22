
public class TimeLogger {
	long time;

	public TimeLogger(){
		time = System.currentTimeMillis();
	}

	public void tick() {
		long currentTime = System.currentTimeMillis();
		System.out.println("Time:"+(currentTime-time)+"ms");
		time = currentTime;
	}
}
