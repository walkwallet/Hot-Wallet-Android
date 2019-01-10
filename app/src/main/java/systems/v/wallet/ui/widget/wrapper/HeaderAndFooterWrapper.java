package systems.v.wallet.ui.widget.wrapper;

import android.view.View;
import android.view.ViewGroup;

import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HeaderAndFooterWrapper extends RecyclerView.Adapter<Holder> {

    private BaseAdapter mInnerAdapter;
    private SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>();
    private SparseArrayCompat<View> mFooterViews = new SparseArrayCompat<>();

    private static final int HEADER_TYPE_BEGIN = 100000;
    private static final int FOOTER_TYPE_BEGIN = 200000;

    public HeaderAndFooterWrapper(BaseAdapter innerAdapter) {
        this.mInnerAdapter = innerAdapter;
    }

    public void addHeaderView(View view) {
        mHeaderViews.put(mHeaderViews.size() + HEADER_TYPE_BEGIN, view);
    }

    public BaseAdapter getInnerAdapter() {
        return mInnerAdapter;
    }


    public void addHeaderView(View view, int index) {
        if (index > mHeaderViews.size()) {
            index = mHeaderViews.size();
        }
        mHeaderViews.put(index + HEADER_TYPE_BEGIN, view);
    }

    public int getHeaderCount() {
        return mHeaderViews.size();
    }

    public void removeHeaderView(int index) {
        mHeaderViews.delete(index + HEADER_TYPE_BEGIN);
    }


    public void addFootView(View view) {
        mFooterViews.put(mFooterViews.size() + FOOTER_TYPE_BEGIN, view);
    }

    public void removeFooter(Object tag) {
        int index = -1;
        for (int i = 0; i < mFooterViews.size(); i++) {
            View v = mFooterViews.get(FOOTER_TYPE_BEGIN + i);
            if (v == null) {
                continue;
            }
            if (tag == v.getTag()) {
                index = FOOTER_TYPE_BEGIN + i;
                break;
            }
        }
        if (index != -1) {
            mFooterViews.delete(index);
        }
    }

    public View getFooterBtTag(Object tag) {
        View view = null;
        for (int i = 0; i < mFooterViews.size(); i++) {
            View v = mFooterViews.get(FOOTER_TYPE_BEGIN + i);
            if (v == null) {
                continue;
            }
            if (tag == v.getTag()) {
                view = v;
                break;
            }
        }
        return view;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderViews.get(viewType) != null) {
            return new Holder(mHeaderViews.get(viewType));
        } else if (mFooterViews.get(viewType) != null) {
            return new Holder(mFooterViews.get(viewType));
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        if (isHeaderPosition(position) || isFooterPosition(position)) {
            return;
        }
        mInnerAdapter.onBindViewHolder(holder, position - mHeaderViews.size());
    }

    @Override
    public int getItemCount() {
        return mHeaderViews.size() + mFooterViews.size() + mInnerAdapter.getItemCount();
    }


    @Override
    public int getItemViewType(int position) {
        if (isHeaderPosition(position)) {
            return mHeaderViews.keyAt(position);
        } else if (isFooterPosition(position)) {
            return mFooterViews.keyAt(position - mHeaderViews.size() - getInnerCount());
        }
        return mInnerAdapter.getItemViewType(position - mHeaderViews.size());
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        WrapperUtils.onAttachedToRecyclerView(mInnerAdapter, recyclerView, new WrapperUtils.SpanSizeCallback() {
            @Override
            public int getSpanSize(GridLayoutManager layoutManager, GridLayoutManager.SpanSizeLookup oldLookup, int position) {
                int viewType = getItemViewType(position);
                if (mHeaderViews.get(viewType) != null) {
                    return layoutManager.getSpanCount();
                } else if (mFooterViews.get(viewType) != null) {
                    return layoutManager.getSpanCount();
                }
                if (oldLookup != null)
                    return oldLookup.getSpanSize(position);
                return 1;
            }
        });
    }

    @Override
    public void onViewAttachedToWindow(Holder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        if (isHeaderPosition(position) || isFooterPosition(position)) {
            WrapperUtils.setFullSpan(holder);
        }
    }

    private boolean isHeaderPosition(int position) {
        return position < mHeaderViews.size();
    }

    private boolean isFooterPosition(int position) {
        return position >= mHeaderViews.size() + getInnerCount();
    }

    private int getInnerCount() {
        return mInnerAdapter.getItemCount();
    }
}

