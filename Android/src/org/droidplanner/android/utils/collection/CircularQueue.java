package org.droidplanner.android.utils.collection;

import java.util.Arrays;

/**
 * Created by Fredia Huya-Kouadio on 1/23/15.
 */
public class CircularQueue<T> {

    private final T[] buffer;
    private int bufferSize;
    private int bufferIndex;

    public CircularQueue(int capacity) {
        buffer = (T[]) new Object[capacity];
        bufferSize = 0;
        bufferIndex = 0;
    }

    private int getAndIncrementIndex(){
        int index = bufferIndex;
        bufferIndex = bufferIndex++ % buffer.length;
        return index;
    }

    private int decrementAndGetIndex(){
        return bufferIndex-- % buffer.length;
    }

    public int capacity(){
        return buffer.length;
    }

    public void clear(){
        bufferSize = 0;
        bufferIndex = 0;

        for(int i = 0; i < buffer.length; i++){
            buffer[i] = null;
        }
    }

    public int size(){
        return bufferSize;
    }

    public boolean isEmpty(){
        return bufferSize == 0;
    }

    public void add(T item) {
        buffer[getAndIncrementIndex()] = item;
    }

    public T poll() {
        if(isEmpty())
            return null;

        int index = decrementAndGetIndex();
        T item = buffer[index];
        buffer[index] = null;
        return item;
    }
}
