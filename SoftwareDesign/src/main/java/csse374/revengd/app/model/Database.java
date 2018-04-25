package csse374.revengd.app.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import soot.Scene;

public class Database {
	private Map<String, String> config;
	private Map<String, Object> storage;

	public Database(Map<String, String> config, Scene scene) {
		this.storage = new HashMap<>();
		this.config = config;
		putObject("scene", scene);
	}

	public void putObject(String key, Object value) {
		this.storage.put(key, value);
	}

	public <T extends Object> T getFilteredObjects(Class<T> filterType, String key) {
		return filterType.cast(this.storage.get(key));
	}

	public <T extends Object> Collection<T> castElements(Class<T> elemType, Collection<Object> col) {
		return col.stream().map(e -> elemType.cast(e)).collect(Collectors.toList());
	}

	public Map<String, String> getConfig() {
		return this.config;
	}

	public void setConfig(Map<String, String> config) {
		this.config = config;
	}

}
