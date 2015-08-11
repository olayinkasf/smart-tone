/*
 * Copyright 2015
 *
 * Olayinka S. Folorunso <mail@olayinkasf.com>
 * http://olayinkasf.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.olayinka.smart.tone.model;

import android.util.Log;

import java.util.*;

/**
 * Created by Olayinka on 6/19/2015.
 */
public class ListenableHashSet<E> extends HashSet<E> {

    boolean mCollate = true;

    public ListenableHashSet() {
        super();
    }

    public ListenableHashSet(int length) {
        super(length);
    }

    public void setCollate(boolean collate) {
        mCollate = collate;
    }

    public static interface HashSetListener {
        public void onDataSetChanged();
    }

    HashMap<String, HashSetListener> mListeners = new HashMap<>(10);

    @Override
    public boolean remove(Object object) {
        if (super.remove(object)) {
            notifyListeners();
            return true;
        }
        return false;
    }

    @Override
    public boolean add(E object) {
        if (super.add(object)) {
            notifyListeners();
            return true;
        }
        return false;
    }

    /**
     * Attempts to add all of the objects contained in {@code collection}
     * to the contents of this {@code Collection} (optional). This implementation
     * iterates over the given {@code Collection} and calls {@code add} for each
     * element. If any of these calls return {@code true}, then {@code true} is
     * returned as result of this method call, {@code false} otherwise. If this
     * {@code Collection} does not support adding elements, an {@code
     * UnsupportedOperationException} is thrown.
     * <p/>
     * If the passed {@code Collection} is changed during the process of adding elements
     * to this {@code Collection}, the behavior depends on the behavior of the passed
     * {@code Collection}.
     *
     * @param collection the collection of objects.
     * @return {@code true} if this {@code Collection} is modified, {@code false}
     * otherwise.
     * @throws UnsupportedOperationException if adding to this {@code Collection} is not supported.
     * @throws ClassCastException            if the class of an object is inappropriate for this
     *                                       {@code Collection}.
     * @throws IllegalArgumentException      if an object cannot be added to this {@code Collection}.
     * @throws NullPointerException          if {@code collection} is {@code null}, or if it contains
     *                                       {@code null} elements and this {@code Collection} does not support
     *                                       such elements.
     */
    public boolean addAll(Collection<? extends E> collection) {
        boolean result = false;
        Iterator<? extends E> it = collection.iterator();
        while (it.hasNext()) {
            if (super.add(it.next())) {
                result = true;
                if (!mCollate) {
                    notifyListeners();
                }
            }
        }
        if (mCollate && result) {
            notifyListeners();
        }
        return result;
    }

    private void notifyListeners() {
        Log.wtf("notifyListeners", "" + mListeners.size());
        for (Map.Entry<String, HashSetListener> listener : mListeners.entrySet()) {
            listener.getValue().onDataSetChanged();
        }
    }

    public void removeListener(String key) {
        mListeners.remove(key);
    }

    public void addListener(String key, HashSetListener listener) {
        mListeners.put(key, listener);
        Log.wtf("addListener", listener.toString());
    }


    /**
     * Removes all occurrences in this collection which are contained in the
     * specified collection.
     *
     * @param collection the collection of objects to remove.
     * @return {@code true} if this collection was modified, {@code false}
     * otherwise.
     * @throws UnsupportedOperationException if removing from this collection is not supported.
     */
    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean result = false;
        if (size() <= collection.size()) {
            Iterator<?> it = iterator();
            while (it.hasNext()) {
                if (collection.contains(it.next())) {
                    it.remove();
                    result = true;
                    if (!mCollate) {
                        notifyListeners();
                    }
                }
            }
        } else {
            Iterator<?> it = collection.iterator();
            while (it.hasNext()) {
                if (super.remove(it.next())) {
                    result = true;
                    if (!mCollate) {
                        notifyListeners();
                    }
                }
            }
        }
        if (mCollate && result) {
            notifyListeners();
        }
        return result;
    }


    /**
     * Removes all objects from this {@code Collection} that are not also found in the
     * {@code Collection} passed (optional). After this method returns this {@code Collection}
     * will only contain elements that also can be found in the {@code Collection}
     * passed to this method.
     * <p/>
     * This implementation iterates over this {@code Collection} and tests for each
     * element {@code e} returned by the iterator, whether it is contained in
     * the specified {@code Collection}. If this test is negative, then the {@code
     * remove} method is called on the iterator. If the iterator does not
     * support removing elements, an {@code UnsupportedOperationException} is
     * thrown.
     *
     * @param collection the collection of objects to retain.
     * @return {@code true} if this {@code Collection} is modified, {@code false}
     * otherwise.
     * @throws UnsupportedOperationException if removing from this {@code Collection} is not supported.
     * @throws ClassCastException            if one or more elements of {@code collection}
     *                                       isn't of the correct type.
     * @throws NullPointerException          if {@code collection} contains at least one
     *                                       {@code null} element and this {@code Collection} doesn't support
     *                                       {@code null} elements.
     * @throws NullPointerException          if {@code collection} is {@code null}.
     */
    public boolean retainAll(Collection<?> collection) {
        boolean result = false;
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            if (!collection.contains(it.next())) {
                it.remove();
                result = true;
                if (!mCollate) {
                    notifyListeners();
                }
            }
        }
        if (mCollate && result) {
            notifyListeners();
        }
        return result;
    }

}
