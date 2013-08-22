package helpers;

public class TimeLogger {
	long time;

	public TimeLogger(){
		time = System.currentTimeMillis();
	}

	public void tick() {
		tick("");
	}

	public void tick(String string) {
		long currentTime = System.currentTimeMillis();
		System.out.println(string+":"+(currentTime-time)+"ms");
		time = currentTime;		
	}
}
