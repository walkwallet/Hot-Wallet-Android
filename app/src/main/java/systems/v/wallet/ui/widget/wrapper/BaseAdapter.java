package systems.v.wallet.ui.widget.wrapper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseAdapter<T> extends RecyclerView.Adapter<Holder> {

    protected List<T> mData = new ArrayList<>();
    protected Context mContext;
    protected OnItemClickListener mOnItemClickListener;
    protected LayoutInflater mInflater;
    protected onItemLongClickListener mItemOnLongClickListener;

    public BaseAdapter(List<T> data, Context context) {
        if (data != null) {
            this.mData = data;
        }
        this.mData = data;
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layId = setLayId(parent, viewType);
        if (layId == 0) {
            try {
                throw new Exception("warning , please set layout first!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ViewDataBinding viewDataBinding = DataBindingUtil.inflate(mInflater, layId, parent, false);
        return new Holder(viewDataBinding);
    }

    @Override
    public void onBindViewHolder(final Holder holder, int position) {
        final int index = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClickListener(holder.itemView, index);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mItemOnLongClickListener != null) {
                    mItemOnLongClickListener.onLongClick(holder.itemView, index);
                    return true;
                }
                return false;
            }
        });
        setViewWithHolder(holder, position);
        setViews(holder.getBinding(), position);
        holder.getBinding().executePendingBindings();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setViewWithHolder(RecyclerView.ViewHolder holder, int position) {

    }

    public interface OnItemClickListener {
        void onItemClickListener(View view, int position);
    }

    public abstract int setLayId(ViewGroup parent, int viewType);

    public abstract void setViews(ViewDataBinding viewDataBinding, int position);

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface onItemLongClickListener {
        void onLongClick(View view, int position);
    }

    public void setOnItemLongClickListener(onItemLongClickListener longClickListener) {
        this.mItemOnLongClickListener = longClickListener;
    }
}
