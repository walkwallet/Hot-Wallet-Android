package systems.v.wallet.ui.view.transaction.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

import systems.v.wallet.R;

public class SuperNodeDetailAdapter extends BaseAdapter {
    private Context mContext;
    private List<SuperNodeInfo> mDatas;

    public SuperNodeDetailAdapter(Context mContext, List<SuperNodeInfo> mDatas) {
        this.mContext = mContext;
        this.mDatas = mDatas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return mDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.item_node, null);
        TextView TvNodeType = (TextView) view.findViewById(R.id.tv_node_type);
        TextView TvNodeNameAddr = (TextView) view.findViewById(R.id.tv_node_name_addr);

        SuperNodeInfo nodeInfo = mDatas.get(i);
        String nodeName = "";
        if (!nodeInfo.isSubNode) {
            TvNodeType.setText("Supernode    ");
            nodeName = nodeInfo.superNodeName;
        } else {
            TvNodeType.setText("Subnode        ");
            nodeName = nodeInfo.subNodeName;
        }

        String nodeAddr = nodeInfo.address;
        if (nodeAddr.length() > 19) {
            TvNodeNameAddr.setText(String.format("%s (%s****%s)", nodeName, nodeAddr.substring(0, 6), nodeAddr.substring(nodeAddr.length() - 6)));
        } else {
            TvNodeNameAddr.setText(String.format("%s (%s)", nodeName, nodeAddr));
        }
        return view;
    }
}
