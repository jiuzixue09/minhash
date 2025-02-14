package com.firstbrave.minhash.util;




public abstract class PriorityQueue<T>  {
	  private T[] heap;
	  private int size;
	  private int maxSize;

	  /** Determines the ordering of objects in this priority queue.  Subclasses
	    must define this one method. */
	  protected abstract boolean lessThan(T a, T b);

	  /** Subclass constructors must call this. */
	  @SuppressWarnings("unchecked")
	  public final void initialize(int maxSize) {
	    size = 0;
	    int heapSize = maxSize + 1;
	    this.heap = (T[]) new Object[heapSize];
	    this.maxSize = maxSize;
	  }

	/**
	   * Adds an Object to a PriorityQueue in log(size) time.
	   * If one tries to add more objects than maxSize from initialize
	   * a RuntimeException (ArrayIndexOutOfBound) is thrown.
	   */
	  public final void put(T element) {
	    size++;
	    heap[size] = element;
	    upHeap();
	  }

	  /**
	   * Adds element to the PriorityQueue in log(size) time if either
	   * the PriorityQueue is not full, or not lessThan(element, top()).
	   * @param element
	   * @return true if element is added, false otherwise.
	   */
	  public boolean insert(T element){
	    if(size < maxSize){
	        put(element);
	        return true;
	    }
	    else if(size > 0 && lessThan(element, top())){
	        heap[1] = element;
	        adjustTop();
	        return true;
	    }
	    else
	        return false;
	   }

	  /** Returns the least element of the PriorityQueue in constant time. */
	  public final T top() {
	    if (size > 0)
	      return heap[1];
	    else
	      return null;
	  }

	  /** Removes and returns the least element of the PriorityQueue in log(size)
	    time. */
	  public final T pop() {
	    if (size > 0) {
	      T result = heap[1];			  // save first value
	      heap[1] = heap[size];			  // move last to first
	      heap[size] = null;			  // permit GC of objects
	      size--;
	      downHeap();				  // adjust heap
	      return result;
	    } else
	      return null;
	  }

	  /** Should be called when the Object at top changes values.  Still log(n)
	   * worst case, but it's at least twice as fast to 

	   *  { pq.top().change(); pq.adjustTop(); }
	   * 

	 instead of 

	   *  { o = pq.pop(); o.change(); pq.push(o); }
	   * 


	   */
	  public final void adjustTop() {
	    downHeap();
	  }


	  /** Returns the number of elements currently stored in the PriorityQueue. */
	  public final int size() {
	    return size;
	  }

	  /** Removes all entries from the PriorityQueue. */
	  public final void clear() {
	    for (int i = 0; i <= size; i++)
	      heap[i] = null;
	    size = 0;
	  }

	  private final void upHeap() {
	    int i = size;
	    T node = heap[i];			  // save bottom node
	    int j = i >>> 1;
	    while (j > 0 && lessThan(node, heap[j])) {
	      heap[i] = heap[j];			  // shift parents down
	      i = j;
	      j = j >>> 1;
	    }
	    heap[i] = node;				  // install saved node
	  }

	  private final void downHeap() {
	    int i = 1;
	    T node = heap[i];			  // save top node
	    int j = i << 1;				  // find smaller child
	    int k = j + 1;
	    if (k <= size && lessThan(heap[k], heap[j])) {
	      j = k;
	    }
	    while (j <= size && lessThan(heap[j], node)) {
	      heap[i] = heap[j];			  // shift up child
	      i = j;
	      j = i << 1;
	      k = j + 1;
	      if (k <= size && lessThan(heap[k], heap[j])) {
		j = k;
	      }
	    }
	    heap[i] = node;				  // install saved node
	  }
	}