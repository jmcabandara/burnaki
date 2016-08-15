package fr.tunaki.stackoverflow.burnaki;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.tunaki.stackoverflow.burnaki.entity.BurninationProgress;
import fr.tunaki.stackoverflow.burnaki.service.BurninationService;
import fr.tunaki.stackoverflow.burnaki.service.BurninationUpdateEvent;
import fr.tunaki.stackoverflow.burnaki.service.BurninationUpdateListener;

@Component
public class BurninationManager implements Closeable, InitializingBean {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BurninationManager.class);
	
	private BurninationService burninationService;
	private ScheduledExecutorService executorService;
	private BurninationManagerProperties properties;
	
	private ExecutorService listenerExecutor = Executors.newCachedThreadPool();
	private List<BurninationUpdateListener> listeners = new ArrayList<>();
	
	private Map<String, List<ScheduledFuture<?>>> tasks = new ConcurrentHashMap<>();
	
	@Autowired
	public BurninationManager(ScheduledExecutorService executorService, BurninationService burninationService, BurninationManagerProperties properties) {
		this.executorService = executorService;
		this.burninationService = burninationService;
		this.properties = properties;
	}
	
	public void addListener(BurninationUpdateListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		burninationService.getTagsInBurnination().forEach(this::scheduleTasks);
	}

	public int start(String tag, int roomId, String metaLink) {
		int size = burninationService.start(tag, roomId, metaLink);
		scheduleTasks(tag);
		return size;
	}

	private void scheduleTasks(String tag) {
		LOGGER.info("Scheduling background burnination tasks for tag [{}]", tag);
		int refreshQuestionsEvery = properties.getRefreshQuestionsEvery();
		int refreshProgressEvery = properties.getRefreshProgressEvery();
		tasks.computeIfAbsent(tag, t -> Arrays.asList(
			executorService.scheduleAtFixedRate(() -> {
				List<BurninationUpdateEvent> events = burninationService.update(t, refreshQuestionsEvery);
				listeners.forEach(l -> listenerExecutor.submit(() -> l.onUpdate(events)));
			}, refreshQuestionsEvery, refreshQuestionsEvery, TimeUnit.MINUTES),
			executorService.scheduleAtFixedRate(() -> burninationService.updateProgress(t), refreshProgressEvery, refreshProgressEvery, TimeUnit.MINUTES)
		));
	}
	
	public void updateProgressNow(String tag) {
		burninationService.updateProgress(tag);
	}
	
	public BurninationProgress getProgress(String tag) {
		return burninationService.getProgress(tag);
	}
	
	public Map<Integer, String> getBurnRooms() {
		return burninationService.getBurnRooms();
	}
	
	public void stop(String tag) {
		burninationService.stop(tag);
		List<ScheduledFuture<?>> tagTasks = tasks.remove(tag);
		if (tagTasks != null) {
			tagTasks.forEach(t -> t.cancel(false));
		}
	}
	
	@Override
	public void close() throws IOException {
		executorService.shutdownNow();
		listenerExecutor.shutdownNow();
	}

}