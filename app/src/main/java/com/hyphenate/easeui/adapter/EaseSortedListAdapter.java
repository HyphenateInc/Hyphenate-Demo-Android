package com.hyphenate.easeui.adapter;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class EaseSortedListAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Editor<T> {
        Editor<T> add(T item);

        Editor<T> add(List<T> items);

        Editor<T> remove(T item);

        Editor<T> remove(List<T> items);

        Editor<T> replaceAll(List<T> items);

        Editor<T> removeAll();

        void commit();
    }

    public interface Filter<T> {
        boolean test(T item);
    }

    private final LayoutInflater mInflater;
    private final SortedList<T> mSortedList;
    private final Comparator<T> mComparator;

    public EaseSortedListAdapter(Context context, Class<T> itemClass, Comparator<T> comparator) {
        mInflater = LayoutInflater.from(context);
        mComparator = comparator;

        mSortedList = new SortedList<>(itemClass, new SortedListAdapterCallback<T>(this) {
            @Override
            public int compare(T o1, T o2) {
                int result;
                if (areItemsTheSame(o1, o2)) {
                    return 0;
                } else {
                    return mComparator.compare(o1, o2);
                }
            }

            @Override
            public boolean areContentsTheSame(T oldItem, T newItem) {
                return EaseSortedListAdapter.this.areItemContentsTheSame(oldItem, newItem);
            }

            @Override
            public boolean areItemsTheSame(T item1, T item2) {
                return EaseSortedListAdapter.this.areItemsTheSame(item1, item2);
            }
        });

    }

    /**
     * Called by the SortedList to decide whether two object represent the same Item or not.
     * <p>
     * For example, if your items have unique ids, this method should check their equality.
     *
     * @param item1
     * @param item2
     * @return
     */
    protected abstract boolean areItemsTheSame(T item1, T item2);

    /**
     * Called by the SortedList when it wants to check whether two items have the same data
     * or not. SortedList uses this information to decide whether it should call
     * onChanged(int, int) or not.
     * <p>
     * SortedList uses this method to check equality instead of {@link Object#equals(Object)}
     *
     * @param oldItem
     * @param newItem
     * @return
     */
    protected abstract boolean areItemContentsTheSame(T oldItem, T newItem);


    public final Editor<T> edit() {
        return new EditorImpl();
    }

    public final T getItem(int position) {
        return mSortedList.get(position);
    }

    @Override
    public final int getItemCount() {
        return mSortedList.size();
    }

    public final List<T> filter(Filter<T> filter) {
        final List<T> list = new ArrayList<>();
        for (int i = 0, count = mSortedList.size(); i < count; i++) {
            final T item = mSortedList.get(i);
            if (filter.test(item)) {
                list.add(item);
            }
        }
        return list;
    }

    public final T filterOne(Filter<T> filter) {
        for (int i = 0, count = mSortedList.size(); i < count; i++) {
            final T item = mSortedList.get(i);
            if (filter.test(item)) {
                return item;
            }
        }
        return null;
    }

    private interface Action<T> {
        void perform(SortedList<T> list);
    }

    private class EditorImpl implements Editor<T> {

        private final List<Action<T>> mActions = new ArrayList<>();

        @Override
        public Editor<T> add(final T item) {
            mActions.add(new Action<T>() {
                @Override
                public void perform(SortedList<T> list) {
                    mSortedList.add(item);
                }
            });
            return this;
        }

        @Override
        public Editor<T> add(final List<T> items) {
            mActions.add(new Action<T>() {
                @Override
                public void perform(SortedList<T> list) {
                    Collections.sort(items, mComparator);
                    mSortedList.addAll(items);
                }
            });
            return this;
        }

        @Override
        public Editor<T> remove(final T item) {
            mActions.add(new Action<T>() {
                @Override
                public void perform(SortedList<T> list) {
                    mSortedList.remove(item);
                }
            });
            return this;
        }

        @Override
        public Editor<T> remove(final List<T> items) {
            mActions.add(new Action<T>() {
                @Override
                public void perform(SortedList<T> list) {
                    for (T item : items) {
                        mSortedList.remove(item);
                    }
                }
            });
            return this;
        }

        @Override
        public Editor<T> replaceAll(final List<T> items) {
            mActions.add(new Action<T>() {
                @Override
                public void perform(SortedList<T> list) {
                    final List<T> itemsToRemove = filter(new Filter<T>() {
                        @Override
                        public boolean test(T item) {
                            return !items.contains(item);
                        }
                    });

                    for (int i = itemsToRemove.size() - 1; i >= 0; i--) {
                        final T item = itemsToRemove.get(i);
                        mSortedList.remove(item);
                    }
                    mSortedList.addAll(items);
                }
            });
            return this;
        }

        @Override
        public Editor<T> removeAll() {
            mActions.add(new Action<T>() {
                @Override
                public void perform(SortedList<T> list) {
                    mSortedList.clear();
                }
            });
            return this;
        }

        @Override
        public void commit() {
            mSortedList.beginBatchedUpdates();
            for (Action<T> action : mActions) {
                action.perform(mSortedList);
            }
            mSortedList.endBatchedUpdates();
            mActions.clear();
        }
    }

}