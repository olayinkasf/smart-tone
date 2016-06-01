package com.olayinka.smart.tone.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by Olayinka on 8/21/2015.
 */
public class OrderedMediaSet<E> extends ListenableHashSet<E> implements ListenableHashSet.HashSetListener<E> {

    LinkedList<E> mList = new LinkedList<>();
    HashMap<E, Integer> mPositionMap = new HashMap<>(100);

    public OrderedMediaSet() {
    }


    public OrderedMediaSet(int length) {
        super(length);
    }

    @Override
    protected void notifyListeners(E object, int op) {
        onDataSetChanged(object, op);
        super.notifyListeners(object, op);
    }

    @Override
    protected void notifyListeners(Collection<? extends E> objects, int op) {
        onDataSetChanged(objects, op);
        super.notifyListeners(objects, op);
    }

    @Override
    public synchronized void onDataSetChanged(Collection<? extends E> objects, int op) {
        switch (op) {
            case REMOVED:
                HashSet<Integer> positions = new HashSet<>(objects.size());
                int minPos = mList.size();
                int maxPos = -1;

                for (E object : objects) {
                    int position = mPositionMap.get(object);
                    positions.add(position);
                    minPos = Math.min(minPos, position);
                    maxPos = Math.max(maxPos, position);
                }

                for (ListIterator<E> it = mList.listIterator(maxPos + 1); it.hasPrevious(); ) {
                    int position = it.previousIndex();
                    E object = it.previous();
                    if (positions.contains(position)) {
                        it.remove();
                        mPositionMap.remove(object);
                        if (position == minPos)
                            break;
                    }
                }

                for (ListIterator<E> it = mList.listIterator(mList.size()); it.hasPrevious(); ) {
                    int position = it.previousIndex();
                    E object = it.previous();
                    mPositionMap.put(object, position);
                    if (position == minPos)
                        break;
                }

                break;
            case ADDED:
                for (E object : objects) {
                    mPositionMap.put(object, mList.size());
                    mList.add(object);
                }
                break;
        }
    }

    @Override
    public synchronized void onDataSetChanged(E e, int op) {
        switch (op) {
            case REMOVED:
                int pos = mPositionMap.get(e);
                mList.remove(pos);
                for (ListIterator<E> it = mList.listIterator(pos); it.hasNext(); ) {
                    int position = it.nextIndex();
                    E object = it.next();
                    mPositionMap.put(object, position);
                }
                break;
            case ADDED:
                mPositionMap.put(e, mList.size());
                mList.add(e);
                break;
        }
    }

    public LinkedList<E> getList() {
        return (LinkedList<E>) mList.clone();
    }

    public void changePosition(int from, int to) {
        E e = mList.remove(from);
        mList.add(to, e);
        int start = Math.min(from, to);
        int end = Math.max(from, to);
        for (ListIterator<E> it = mList.listIterator(start); it.hasNext(); ) {
            int position = it.nextIndex();
            E object = it.next();
            mPositionMap.put(object, position);
            if (position == end)
                break;
        }
    }

    public boolean remove(int which) {
        E e = mList.get(which);
        return remove(e);
    }
}
