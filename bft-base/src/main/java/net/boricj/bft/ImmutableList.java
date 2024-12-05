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

public interface ImmutableList<T> extends List<T> {

	abstract List<T> getElements();

	@Override
	public default boolean add(T e) {
		throw new UnsupportedOperationException("Unimplemented method 'add'");
	}

	@Override
	public default void add(int index, T element) {
		throw new UnsupportedOperationException("Unimplemented method 'add'");
	}

	@Override
	public default boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException("Unimplemented method 'addAll'");
	}

	@Override
	public default boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException("Unimplemented method 'addAll'");
	}

	@Override
	public default void addFirst(T e) {
		throw new UnsupportedOperationException("Unimplemented method 'addFirst'");
	}

	@Override
	public default void addLast(T e) {
		throw new UnsupportedOperationException("Unimplemented method 'addLast'");
	}

	@Override
	public default void clear() {
		throw new UnsupportedOperationException("Unimplemented method 'clear'");
	}

	@Override
	public default boolean contains(Object o) {
		return getElements().contains(o);
	}

	@Override
	public default boolean containsAll(Collection<?> c) {
		return getElements().containsAll(c);
	}

	@Override
	public default T get(int index) {
		return getElements().get(index);
	}

	@Override
	public default T getFirst() {
		return getElements().getFirst();
	}

	@Override
	public default T getLast() {
		return getElements().getLast();
	}

	@Override
	public default int indexOf(Object o) {
		return getElements().indexOf(o);
	}

	@Override
	public default boolean isEmpty() {
		return getElements().isEmpty();
	}

	@Override
	public default Iterator<T> iterator() {
		return getElements().iterator();
	}

	@Override
	public default int lastIndexOf(Object o) {
		return getElements().lastIndexOf(o);
	}

	@Override
	public default ListIterator<T> listIterator() {
		return getElements().listIterator();
	}

	@Override
	public default ListIterator<T> listIterator(int index) {
		return getElements().listIterator(index);
	}

	@Override
	public default boolean remove(Object o) {
		throw new UnsupportedOperationException("Unimplemented method 'remove'");
	}

	@Override
	public default T remove(int index) {
		throw new UnsupportedOperationException("Unimplemented method 'remove'");
	}

	@Override
	public default boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Unimplemented method 'removeAll'");
	}

	@Override
	public default T removeFirst() {
		throw new UnsupportedOperationException("Unimplemented method 'removeFirst'");
	}

	@Override
	public default T removeLast() {
		throw new UnsupportedOperationException("Unimplemented method 'removeLast'");
	}

	@Override
	public default void replaceAll(UnaryOperator<T> operator) {
		throw new UnsupportedOperationException("Unimplemented method 'replaceAll'");
	}

	@Override
	public default boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Unimplemented method 'retainAll'");
	}

	@Override
	public default List<T> reversed() {
		return getElements().reversed();
	}

	@Override
	public default T set(int index, T element) {
		throw new UnsupportedOperationException("Unimplemented method 'set'");
	}

	@Override
	public default int size() {
		return getElements().size();
	}

	@Override
	public default void sort(Comparator<? super T> c) {
		throw new UnsupportedOperationException("Unimplemented method 'sort'");
	}

	@Override
	public default Spliterator<T> spliterator() {
		return getElements().spliterator();
	}

	@Override
	public default List<T> subList(int fromIndex, int toIndex) {
		return getElements().subList(fromIndex, toIndex);
	}

	@Override
	public default Object[] toArray() {
		return getElements().toArray();
	}

	@Override
	public default <U> U[] toArray(U[] a) {
		return getElements().toArray(a);
	}

	@Override
	public default Stream<T> parallelStream() {
		return getElements().parallelStream();
	}

	@Override
	public default boolean removeIf(Predicate<? super T> filter) {
		throw new UnsupportedOperationException("Unimplemented method 'removeIf'");
	}

	@Override
	public default Stream<T> stream() {
		return getElements().stream();
	}

	@Override
	public default <U> U[] toArray(IntFunction<U[]> generator) {
		return getElements().toArray(generator);
	}

	@Override
	public default void forEach(Consumer<? super T> action) {
		getElements().forEach(action);
	}
}
