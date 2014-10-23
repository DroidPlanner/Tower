package org.droidplanner.desktop.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Handler implements
		org.droidplanner.core.drone.DroneInterfaces.Handler {

	private final ScheduledExecutorService scheduler = Executors
			.newSingleThreadScheduledExecutor();
	private final Map<Runnable, ScheduledFuture<?>> futureThreads = new HashMap<Runnable, ScheduledFuture<?>>();

	@Override
	public void removeCallbacks(Runnable thread) {
		if (futureThreads.containsKey(thread)) {
			boolean mayInterruptIfRunning = false;
			futureThreads.get(thread).cancel(mayInterruptIfRunning);
		}
	}

	@Override
	public void post(Runnable thread) {
		scheduler.execute(thread);
	}

	@Override
	public void postDelayed(Runnable thread, long timeout) {
		ScheduledFuture<?> future = scheduler.schedule(thread, timeout,
				TimeUnit.MILLISECONDS);
		futureThreads.put(thread, future);
	}
}
