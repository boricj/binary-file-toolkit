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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public interface IndirectList<T> extends List<T> {
	abstract List<T> getElements();

	@Override
	default boolean add(T e) {
		return getElements().add(e);
	}

	@Override
	default void add(int index, T element) {
		getElements().add(index, element);
	}

	@Override
	default boolean addAll(Collection<? extends T> c) {
		return getElements().addAll(c);
	}

	@Override
	default boolean addAll(int index, Collection<? extends T> c) {
		return getElements().addAll(index, c);
	}

	@Override
	default void addFirst(T e) {
		getElements().addFirst(e);
	}

	@Override
	default void addLast(T e) {
		getElements().addLast(e);
	}

	@Override
	default void clear() {
		getElements().clear();
	}

	@Override
	default boolean contains(Object o) {
		return getElements().contains(o);
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return getElements().containsAll(c);
	}

	@Override
	default T get(int index) {
		return getElements().get(index);
	}

	@Override
	default T getFirst() {
		return getElements().getFirst();
	}

	@Override
	default T getLast() {
		return getElements().getLast();
	}

	@Override
	default int indexOf(Object o) {
		return getElements().indexOf(o);
	}

	@Override
	default boolean isEmpty() {
		return getElements().isEmpty();
	}

	@Override
	default Iterator<T> iterator() {
		return getElements().iterator();
	}

	@Override
	default int lastIndexOf(Object o) {
		return getElements().lastIndexOf(o);
	}

	@Override
	default ListIterator<T> listIterator() {
		return getElements().listIterator();
	}

	@Override
	default ListIterator<T> listIterator(int index) {
		return getElements().listIterator(index);
	}

	@Override
	default boolean remove(Object o) {
		return getElements().remove(o);
	}

	@Override
	default T remove(int index) {
		return getElements().remove(index);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return getElements().removeAll(c);
	}

	@Override
	default T removeFirst() {
		return getElements().removeFirst();
	}

	@Override
	default T removeLast() {
		return getElements().removeLast();
	}

	@Override
	default void replaceAll(UnaryOperator<T> operator) {
		getElements().replaceAll(operator);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return getElements().retainAll(c);
	}

	@Override
	default List<T> reversed() {
		return getElements().reversed();
	}

	@Override
	default T set(int index, T element) {
		return getElements().set(index, element);
	}

	@Override
	default int size() {
		return getElements().size();
	}

	@Override
	default void sort(Comparator<? super T> c) {
		getElements().sort(c);
	}

	@Override
	default Spliterator<T> spliterator() {
		return getElements().spliterator();
	}

	@Override
	default List<T> subList(int fromIndex, int toIndex) {
		return getElements().subList(fromIndex, toIndex);
	}

	@Override
	default Object[] toArray() {
		return getElements().toArray();
	}

	@Override
	default <V> V[] toArray(V[] a) {
		return getElements().toArray(a);
	}

	@Override
	default Stream<T> parallelStream() {
		return getElements().parallelStream();
	}

	@Override
	default boolean removeIf(Predicate<? super T> filter) {
		return getElements().removeIf(filter);
	}

	@Override
	default Stream<T> stream() {
		return getElements().stream();
	}

	@Override
	default <V> V[] toArray(IntFunction<V[]> generator) {
		return getElements().toArray(generator);
	}

	@Override
	default void forEach(Consumer<? super T> action) {
		getElements().forEach(action);
	}
}
