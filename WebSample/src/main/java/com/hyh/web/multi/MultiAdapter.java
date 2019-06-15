package com.hyh.web.multi;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Administrator
 * @description
 * @data 2017/5/18
 */
public class MultiAdapter extends RecyclerView.Adapter<ItemHolder> {

    private List<MultiModule> mModuleList = new ArrayList<>();

    private SparseArray<ItemTypeInfo> mTypeInfoArray = new SparseArray<>();

    private Context mContext;

    public MultiAdapter(Context context) {
        this.mContext = context;
    }

    public <T> void addMultiModule(MultiModule<T> multiModule) {
        multiModule.setup(mContext, mMultiAdapterClient);
        mModuleList.add(multiModule);
    }

    public <T> void addMultiModule(MultiModule<T> multiModule, int index) {
        int size = mModuleList.size();
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
        multiModule.setup(mContext, mMultiAdapterClient);
        mModuleList.add(index, multiModule);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemTypeInfo itemTypeInfo = mTypeInfoArray.get(viewType);
        ItemHolder itemHolder;
        if (itemTypeInfo != null) {
            itemHolder = itemTypeInfo.multiModule.onCreateViewHolder(parent, itemTypeInfo.itemType);
        } else {
            itemHolder = new EmptyHolder(new View(mContext));
        }
        return itemHolder;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        for (MultiModule multiModule : mModuleList) {
            int itemCount = multiModule.getItemCount();
            if (position <= itemCount - 1) {
                multiModule.onBindViewHolder(holder, position);
                return;
            } else {
                position -= itemCount;
            }
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        for (MultiModule multiModule : mModuleList) {
            itemCount += multiModule.getItemCount();
        }
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == RecyclerView.NO_POSITION) {
            return super.getItemViewType(position);
        }
        for (MultiModule multiModule : mModuleList) {
            int itemCount = multiModule.getItemCount();
            if (position <= itemCount - 1) {
                int itemViewType = multiModule.getItemViewType(position);
                int hashCode = System.identityHashCode(multiModule);
                String hashStr = String.valueOf(hashCode) + "-" + itemViewType;

                int realItemViewType = hashStr.hashCode();
                if (mTypeInfoArray.get(realItemViewType) == null) {
                    mTypeInfoArray.put(realItemViewType, new ItemTypeInfo(multiModule, itemViewType));
                }
                return realItemViewType;
            } else {
                position -= itemCount;
            }
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (position == RecyclerView.NO_POSITION) {
                        return 1;
                    }
                    for (MultiModule multiModule : mModuleList) {
                        int itemCount = multiModule.getItemCount();
                        if (position <= itemCount - 1) {
                            return multiModule.getSpanSize(gridManager.getSpanCount(), position);
                        } else {
                            position -= itemCount;
                        }
                    }
                    return 1;
                }
            });
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }


    @Override
    public void onViewAttachedToWindow(ItemHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (isStaggeredGridLayout(holder)) {
            handleLayoutIsStaggeredGridLayout(holder, holder.getLayoutPosition());
        }
        holder.onViewAttachedToWindow();
    }

    @Override
    public void onViewDetachedFromWindow(ItemHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onViewDetachedFromWindow();
    }

    private boolean isStaggeredGridLayout(ItemHolder holder) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        return layoutParams != null && layoutParams instanceof StaggeredGridLayoutManager.LayoutParams;
    }

    private void handleLayoutIsStaggeredGridLayout(ItemHolder holder, int position) {
        for (MultiModule multiModule : mModuleList) {
            int itemCount = multiModule.getItemCount();
            if (position <= itemCount - 1) {
                break;
            } else {
                position -= itemCount;
            }
        }
        if (holder.isFullSpan(position)) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            p.setFullSpan(true);
        }
    }

    @Override
    public void onViewRecycled(ItemHolder holder) {
        super.onViewRecycled(holder);
        holder.onRecycled();
    }

    private MultiModule.MultiAdapterClient mMultiAdapterClient = new MultiModule.MultiAdapterClient() {

        @Override
        public int getModulePosition(MultiModule module) {
            return mModuleList.indexOf(module);
        }

        @Override
        public void notifyDataSetChanged(int modulePosition, int oldSize, int currentSize) {
            int positionStart = getModuleFirstPosition(modulePosition);
            if (currentSize > oldSize) {
                if (oldSize == 0) {
                    MultiAdapter.this.notifyItemRangeChanged(positionStart, currentSize);
                } else {
                    MultiAdapter.this.notifyItemRangeChanged(positionStart, oldSize);
                    MultiAdapter.this.notifyItemRangeInserted(positionStart + oldSize, currentSize - oldSize);
                }
            } else {
                MultiAdapter.this.notifyItemRangeChanged(positionStart, currentSize);
                MultiAdapter.this.notifyItemRangeRemoved(positionStart + currentSize, oldSize - currentSize);
            }
        }

        @Override
        public void notifyItemRangeInserted(int modulePosition, int insertPosition, int size) {
            int positionStart = getModuleFirstPosition(modulePosition) + insertPosition;
            MultiAdapter.this.notifyItemRangeInserted(positionStart, size);
        }

        @Override
        public void notifyItemRemoved(int modulePosition, int index) {
            int removePosition = getModuleFirstPosition(modulePosition) + index;
            MultiAdapter.this.notifyItemRemoved(removePosition);
        }

        @Override
        public void notifyItemChanged(int modulePosition, int index) {
            int updatePosition = getModuleFirstPosition(modulePosition) + index;
            MultiAdapter.this.notifyItemChanged(updatePosition);
        }

        @Override
        public void notifyItemRangeChanged(int modulePosition, int startIndex, int itemCount) {
            int updatePosition = getModuleFirstPosition(modulePosition) + startIndex;
            MultiAdapter.this.notifyItemRangeChanged(updatePosition, itemCount);
        }
    };

    private int getModuleFirstPosition(int modulePosition) {
        int positionStart = 0;
        for (int i = 0; i < modulePosition; i++) {
            positionStart += mModuleList.get(i).getItemCount();
        }
        return positionStart;
    }

    public int getModuleFirstPosition(MultiModule multiModule) {
        if (multiModule == null || multiModule.getItemCount() <= 0) {
            return -1;
        }
        int modulePosition = mModuleList.indexOf(multiModule);
        if (modulePosition < 0) {
            return -1;
        } else {
            return getModuleFirstPosition(modulePosition);
        }
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + mModuleList.size();
    }
}
