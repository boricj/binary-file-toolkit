/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.boricj.bft;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface IndirectNavigableMap<K, V> extends NavigableMap<K, V> {
	public NavigableMap<K, V> getElements();

	@Override
	default Entry<K, V> ceilingEntry(K key) {
		return getElements().ceilingEntry(key);
	}

	@Override
	default K ceilingKey(K key) {
		return getElements().ceilingKey(key);
	}

	@Override
	default NavigableSet<K> descendingKeySet() {
		return getElements().descendingKeySet();
	}

	@Override
	default NavigableMap<K, V> descendingMap() {
		return getElements().descendingMap();
	}

	@Override
	default Entry<K, V> firstEntry() {
		return getElements().firstEntry();
	}

	@Override
	default Entry<K, V> floorEntry(K key) {
		return getElements().floorEntry(key);
	}

	@Override
	default K floorKey(K key) {
		return getElements().floorKey(key);
	}

	@Override
	default SortedMap<K, V> headMap(K toKey) {
		return getElements().headMap(toKey);
	}

	@Override
	default NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
		return getElements().headMap(toKey, inclusive);
	}

	@Override
	default Entry<K, V> higherEntry(K key) {
		return getElements().higherEntry(key);
	}

	@Override
	default K higherKey(K key) {
		return getElements().higherKey(key);
	}

	@Override
	default Entry<K, V> lastEntry() {
		return getElements().lastEntry();
	}

	@Override
	default Entry<K, V> lowerEntry(K key) {
		return getElements().lowerEntry(key);
	}

	@Override
	default K lowerKey(K key) {
		return getElements().lowerKey(key);
	}

	@Override
	default NavigableSet<K> navigableKeySet() {
		return getElements().navigableKeySet();
	}

	@Override
	default Entry<K, V> pollFirstEntry() {
		return getElements().pollFirstEntry();
	}

	@Override
	default Entry<K, V> pollLastEntry() {
		return getElements().pollLastEntry();
	}

	@Override
	default NavigableMap<K, V> reversed() {
		return getElements().reversed();
	}

	@Override
	default SortedMap<K, V> subMap(K fromKey, K toKey) {
		return getElements().subMap(fromKey, toKey);
	}

	@Override
	default NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
		return getElements().subMap(fromKey, fromInclusive, toKey, toInclusive);
	}

	@Override
	default SortedMap<K, V> tailMap(K fromKey) {
		return getElements().tailMap(fromKey);
	}

	@Override
	default NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
		return getElements().tailMap(fromKey, inclusive);
	}

	@Override
	default Comparator<? super K> comparator() {
		return getElements().comparator();
	}

	@Override
	default Set<Entry<K, V>> entrySet() {
		return getElements().entrySet();
	}

	@Override
	default K firstKey() {
		return getElements().firstKey();
	}

	@Override
	default Set<K> keySet() {
		return getElements().keySet();
	}

	@Override
	default K lastKey() {
		return getElements().lastKey();
	}

	@Override
	default V putFirst(K k, V v) {
		return getElements().putFirst(k, v);
	}

	@Override
	default V putLast(K k, V v) {
		return getElements().putLast(k, v);
	}

	@Override
	default Collection<V> values() {
		return getElements().values();
	}

	@Override
	default SequencedSet<Entry<K, V>> sequencedEntrySet() {
		return getElements().sequencedEntrySet();
	}

	@Override
	default SequencedSet<K> sequencedKeySet() {
		return getElements().sequencedKeySet();
	}

	@Override
	default SequencedCollection<V> sequencedValues() {
		return getElements().sequencedValues();
	}

	@Override
	default void clear() {
		getElements().clear();
	}

	@Override
	default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return getElements().compute(key, remappingFunction);
	}

	@Override
	default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return getElements().computeIfAbsent(key, mappingFunction);
	}

	@Override
	default V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return getElements().computeIfPresent(key, remappingFunction);
	}

	@Override
	default boolean containsKey(Object key) {
		return getElements().containsKey(key);
	}

	@Override
	default boolean containsValue(Object value) {
		return getElements().containsValue(value);
	}

	@Override
	default void forEach(BiConsumer<? super K, ? super V> action) {
		getElements().forEach(action);
	}

	@Override
	default V get(Object key) {
		return getElements().get(key);
	}

	@Override
	default V getOrDefault(Object key, V defaultValue) {
		return getElements().getOrDefault(key, defaultValue);
	}

	@Override
	default boolean isEmpty() {
		return getElements().isEmpty();
	}

	@Override
	default V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return getElements().merge(key, value, remappingFunction);
	}

	@Override
	default V put(K key, V value) {
		return getElements().put(key, value);
	}

	@Override
	default void putAll(Map<? extends K, ? extends V> m) {
		getElements().putAll(m);
	}

	@Override
	default V putIfAbsent(K key, V value) {
		return getElements().putIfAbsent(key, value);
	}

	@Override
	default V remove(Object key) {
		return getElements().remove(key);
	}

	@Override
	default boolean remove(Object key, Object value) {
		return getElements().remove(key, value);
	}

	@Override
	default V replace(K key, V value) {
		return getElements().replace(key, value);
	}

	@Override
	default boolean replace(K key, V oldValue, V newValue) {
		return getElements().replace(key, oldValue, newValue);
	}

	@Override
	default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		getElements().replaceAll(function);
	}

	@Override
	default int size() {
		return getElements().size();
	}
}
