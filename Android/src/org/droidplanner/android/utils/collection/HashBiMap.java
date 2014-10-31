package org.droidplanner.android.utils.collection;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by fhuya on 9/15/14.
 */
public class HashBiMap<K, V> {

	private final HashMap<K, V> mKeyToValueMap = new HashMap<K, V>();
	private final HashMap<V, K> mValueToKeyMap = new HashMap<V, K>();

	public void put(K key, V value) {
		mKeyToValueMap.put(key, value);
		mValueToKeyMap.put(value, key);
	}

	public void removeKey(K key) {
		final V value = mKeyToValueMap.get(key);
		if (value != null) {
			mKeyToValueMap.remove(key);
			mValueToKeyMap.remove(value);
		}
	}

	public void removeValue(V value) {
		final K key = mValueToKeyMap.get(value);
		if (key != null) {
			mValueToKeyMap.remove(value);
			mKeyToValueMap.remove(key);
		}
	}

	public V getValue(K key) {
		return mKeyToValueMap.get(key);
	}

	public K getKey(V value) {
		return mValueToKeyMap.get(value);
	}

	public void clear() {
		mKeyToValueMap.clear();
		mValueToKeyMap.clear();
	}

	public Set<K> keySet() {
		return mKeyToValueMap.keySet();
	}

	public Set<V> valueSet() {
		return mValueToKeyMap.keySet();
	}
}
