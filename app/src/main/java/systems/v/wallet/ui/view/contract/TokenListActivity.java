package systems.v.wallet.ui.view.contract;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.api.NodeAPI;
import systems.v.wallet.data.api.PublicApi;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.bean.TokenBalanceBean;
import systems.v.wallet.data.bean.publicApi.TokenInfoBean;
import systems.v.wallet.databinding.ActivityTokenListBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.contract.adapter.TokenAdapter;
import systems.v.wallet.ui.widget.wrapper.BaseAdapter;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.SPUtils;
import systems.v.wallet.utils.bus.AppEvent;
import systems.v.wallet.utils.bus.annotation.Subscribe;

public class TokenListActivity extends BaseThemedActivity implements View.OnClickListener{
    public static void launch(Activity from, String publicKey) {
        Intent intent = new Intent(from, TokenListActivity.class);
        intent.putExtra("publicKey", publicKey);
        from.startActivity(intent);
    }

    private final String TAG = TokenListActivity.class.getSimpleName();

    private ActivityTokenListBinding mBinding;
    private TokenAdapter mAdapter;
    private List<Token> mData = new ArrayList<>();

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_create_token:
                CreateTokenActivity.launch(this, mAccount.getPublicKey());
                break;
            case R.id.ll_add_token:
                AddTokenActivity.launch(this, mAccount.getPublicKey());
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_token_list);
        initView();
        initData();
    }

    private void initView(){
        setAppBar(mBinding.toolbar);
        mBinding.setClick(this);
        mAdapter = new TokenAdapter(mData, mActivity);
        mAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(View view, final int position) {
                Log.d("token list", JSON.toJSONString(mData.get(position)));
                List<TokenOperationFragment.Operation> mOperation = new ArrayList<>();
                mOperation.add(new TokenOperationFragment.Operation(R.string.token_list_send, new TokenOperationFragment.Operation.OperationListener() {
                    @Override
                    public void onOperation(TokenOperationFragment dialog) {
                        SendTokenActivity.launch(TokenListActivity.this, mAccount.getPublicKey(), mData.get(position));
                    }
                }));
                mOperation.add(new TokenOperationFragment.Operation(R.string.token_list_info,new TokenOperationFragment.Operation.OperationListener() {
                    @Override
                    public void onOperation(TokenOperationFragment dialog) {
                        TokenInfoActivity.launch(TokenListActivity.this, mAccount.getPublicKey(), mData.get(position));
                    }
                }));
                mOperation.add(new TokenOperationFragment.Operation(R.string.token_list_issue, new TokenOperationFragment.Operation.OperationListener() {
                    @Override
                    public void onOperation(TokenOperationFragment dialog) {
                        IssueActivity.launch(TokenListActivity.this, mAccount.getPublicKey(), mData.get(position));
                    }
                }));
                mOperation.add(new TokenOperationFragment.Operation(R.string.token_list_destroy, new TokenOperationFragment.Operation.OperationListener() {
                    @Override
                    public void onOperation(TokenOperationFragment dialog) {
                        DestroyTokenActivity.launch(TokenListActivity.this, mAccount.getPublicKey(), mData.get(position));
                    }
                }));
                mOperation.add(new TokenOperationFragment.Operation(R.string.token_list_remove, new TokenOperationFragment.Operation.OperationListener() {
                    @Override
                    public void onOperation(TokenOperationFragment dialog) {
                        final String key = Constants.WATCHED_TOKEN.concat(mAccount.getPublicKey());
                        List<Token> tokens = JSON.parseArray(SPUtils.getString(key), Token.class);
                        if(tokens == null){
                            return ;
                        }
                        for (int i=0; i<tokens.size();i++){
                            if(tokens.get(i).getTokenId().equals(mData.get(position).getTokenId())){
                                tokens.remove(i);
                                SPUtils.setString(key, JSON.toJSONString(tokens));
                                mData.remove(position);
                                if (mAdapter != null){
                                    mAdapter.notifyDataSetChanged();
                                }
                                break;
                            }
                        }

                    }
                }));
                new TokenOperationFragment.Builder()
                        .setOperations(mOperation)
                        .setToken(mData.get(position))
                        .create()
                        .show(TokenListActivity.this.getSupportFragmentManager(),"");
            }
        });
        mBinding.rvToken.setLayoutManager(new LinearLayoutManager(mActivity));
        mBinding.rvToken.setAdapter(mAdapter);

    }

    private void initData(){
//        getVsysBalance(mAccount.getAddress());
        mBinding.tvBalance.setText(CoinUtil.formatWithUnit(mAccount.getAvailable()));
        getTokenList();

    }

    private void getTokenList(){
        List<Token> tokens = JSON.parseArray(SPUtils.getString(Constants.WATCHED_TOKEN.concat(mAccount.getPublicKey())), Token.class);
        if (tokens == null){
            return ;
        }

        mData.clear();
        mData.addAll(tokens);
        final NodeAPI nodeApi = RetrofitHelper.getInstance().getNodeAPI();
        final PublicApi publicApi = RetrofitHelper.getInstance().getPublicAPI();

        if(mData != null) {
            //Balance
            Disposable nd = Observable.fromIterable(mData)
                    .flatMap(new Function<Token, Observable<RespBean>>() {
                        @Override
                        public Observable<RespBean> apply(Token token){
                            return nodeApi.tokenBalance(mAccount.getAddress(), token.getTokenId());
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<RespBean>() {
                        @Override
                        public void accept(RespBean resp) throws Exception {
                            if(resp.getCode() == 0){
                                Log.d("TokenBalance", JSON.toJSONString(resp));
                                TokenBalanceBean balance = JSON.parseObject(resp.getData(), TokenBalanceBean.class);
                                for (int j = 0; j < mData.size(); j++) {
                                    if (mData.get(j).getTokenId().equals(balance.getTokenId())) {
                                        mData.get(j).setBalance(balance.getBalance());
                                        mData.get(j).setUnity(balance.getUnity());
                                    }
                                }
                                if (mAdapter != null) {
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e(TAG, throwable.getMessage());

                        }
                    });
            //Official Token Icon & Name
            Disposable pd = Observable.fromIterable(mData)
                    .flatMap(new Function<Token, Observable<TokenInfoBean>>() {
                        @Override
                        public Observable<TokenInfoBean> apply(Token token){
                            Map<String, Object> params = new HashMap<>();
                            params.put("Id", token.getTokenId());
                            params.put("Action", "detail");
                            return publicApi.apiToken(params);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<TokenInfoBean>() {
                        @Override
                        public void accept(TokenInfoBean tokenInfo) throws Exception {
                            for (int j = 0; j < mData.size(); j++) {
                                if (mData.get(j).getTokenId().equals(tokenInfo.getTokenId())) {
                                    mData.get(j).setName(tokenInfo.getName());
                                    mData.get(j).setIcon(tokenInfo.getIconUrl());
                                }
                            }
                            if (mAdapter != null) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e(TAG, throwable.getMessage());

                        }
                    });
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe
    public void onAppEvent(AppEvent event) {
        switch (event.getType()) {
            case TOKEN_ADD:
                getTokenList();
                break;
        }
    }
}
