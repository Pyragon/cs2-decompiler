package com.cryo.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class PeekableIterator<T> implements Iterator<T> {
	private Iterator<T> iterator;
	private T peekedElement;
	private boolean hasPeeked;

	/**
	 * Creates a peekable iterator from any iterator
	 * @param iterator the iterator to wrap
	 */
	public PeekableIterator(Iterator<T> iterator) {
		this.iterator = iterator;
		this.hasPeeked = false;
		this.peekedElement = null;
	}

	/**
	 * Convenience constructor for LinkedList
	 * @param list the LinkedList to create an iterator for
	 */
	public PeekableIterator(LinkedList<T> list) {
		this(list.iterator());
	}

	/**
	 * Returns the next element without advancing the iterator
	 * @return the next element
	 * @throws NoSuchElementException if there are no more elements
	 */
	public T peek() {
		if (!hasPeeked) {
			if (!iterator.hasNext()) {
				throw new NoSuchElementException("No more elements to peek");
			}
			peekedElement = iterator.next();
			hasPeeked = true;
		}
		return peekedElement;
	}

	/**
	 * Returns true if there's a next element to peek at or iterate over
	 */
	@Override
	public boolean hasNext() {
		return hasPeeked || iterator.hasNext();
	}

	/**
	 * Returns the next element and advances the iterator
	 */
	@Override
	public T next() {
		if (hasPeeked) {
			T result = peekedElement;
			hasPeeked = false;
			peekedElement = null;
			return result;
		}
		return iterator.next();
	}

	/**
	 * Removes the last element returned by next() from the underlying collection
	 * Note: This cannot be called after peek() without calling next() first
	 */
	@Override
	public void remove() {
		if (hasPeeked) {
			throw new IllegalStateException("Cannot remove after peek() without calling next() first");
		}
		iterator.remove();
	}
}
