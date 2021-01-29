package systems.v.wallet.ui.view.contract;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.utils.TxUtil;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.data.BaseErrorConsumer;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.api.NodeAPI;
import systems.v.wallet.data.bean.ContractBean;
import systems.v.wallet.data.bean.ContractInfoBean;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.bean.TokenBalanceBean;
import systems.v.wallet.data.bean.TokenBean;
import systems.v.wallet.data.statics.TokenHelper;
import systems.v.wallet.databinding.ActivityTokenListBinding;
import systems.v.wallet.databinding.HeaderTokenListBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.contract.adapter.TokenAdapter;
import systems.v.wallet.ui.view.detail.ReceiveActivity;
import systems.v.wallet.ui.widget.wrapper.BaseAdapter;
import systems.v.wallet.ui.widget.wrapper.HeaderAndFooterWrapper;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.SPUtils;
import systems.v.wallet.utils.ToastUtil;
import systems.v.wallet.utils.bus.AppEvent;
import systems.v.wallet.utils.bus.annotation.Subscribe;
import vsys.Vsys;

public class TokenListActivity extends BaseThemedActivity implements View.OnClickListener{
    public static void launch(Activity from, String publicKey) {
        Intent intent = new Intent(from, TokenListActivity.class);
        intent.putExtra("publicKey", publicKey);
        from.startActivity(intent);
    }

    private final String TAG = TokenListActivity.class.getSimpleName();

    private ActivityTokenListBinding mBinding;
    private HeaderTokenListBinding mHeaderBinding;
    private HeaderAndFooterWrapper mAdapter;
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

        TokenAdapter tokenAdapter = new TokenAdapter(mData, mActivity);
        tokenAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(View view, final int position) {
//                final Token token = mData.get(position);
                List<TokenOperationFragment.Operation> mOperation = new ArrayList<>();
//                if (mAccount.getAddress().equals(token.getMaker())){
//                    mOperation.add(new TokenOperationFragment.Operation(R.string.token_list_supersede, new TokenOperationFragment.Operation.OperationListener() {
//                        @Override
//                        public void onOperation(TokenOperationFragment dialog) {
//
//                        }
//                    }));
//                }
                mOperation.add(new TokenOperationFragment.Operation(R.string.token_list_send, new TokenOperationFragment.Operation.OperationListener() {
                    @Override
                    public void onOperation(TokenOperationFragment dialog) {
                        SendTokenActivity.launch(TokenListActivity.this, mAccount.getPublicKey(), mData.get(position));
                    }
                }));
                mOperation.add(new TokenOperationFragment.Operation(R.string.token_list_receive, new TokenOperationFragment.Operation.OperationListener() {
                    @Override
                    public void onOperation(TokenOperationFragment dialog) {
                        ReceiveActivity.launch(TokenListActivity.this, mAccount.getPublicKey());
                    }
                }));

                mOperation.add(new TokenOperationFragment.Operation(R.string.token_list_info,new TokenOperationFragment.Operation.OperationListener() {
                    @Override
                    public void onOperation(TokenOperationFragment dialog) {
                        TokenInfoActivity.launch(TokenListActivity.this, mAccount.getPublicKey(), mData.get(position));
                    }
                }));
                if(mData.get(position).getIssuer().equals(mAccount.getAddress()) && !mData.get(position).isNft()) {
//                    mOperation.add(new TokenOperationFragment.Operation(R.string.token_list_split, new TokenOperationFragment.Operation.OperationListener() {
//                        @Override
//                        public void onOperation(TokenOperationFragment dialog) {
//                            IssueActivity.launch(TokenListActivity.this, mAccount.getPublicKey(), mData.get(position));
//                        }
//                    }));
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
                }
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
                                handleDataChange();
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

        mAdapter = new HeaderAndFooterWrapper(tokenAdapter);
        mBinding.rvToken.setLayoutManager(new LinearLayoutManager(mActivity));
        mBinding.rvToken.setAdapter(mAdapter);
        mHeaderBinding = DataBindingUtil.inflate(LayoutInflater.from(mActivity), R.layout.header_token_list, mBinding.rvToken, false);
        mHeaderBinding.setClick(this);
        mAdapter.addHeaderView(mHeaderBinding.getRoot());

    }

    private void initData(){
        setHeaderData();
        getTokenList();
        updateTokensInfo();
    }

    private void getTokenList(){
        final String key = Constants.WATCHED_TOKEN.concat(mAccount.getPublicKey());
        String str = SPUtils.getString(key);
        List<Token> tokens = JSON.parseArray(str, Token.class);
        if (tokens == null){
            return ;
        }
        List<Token> verifiedTokens = TokenHelper.getVerifiedFromCache(this, mAccount.getNetwork());
        for (int i=0; i < tokens.size(); i++) {
            for (Token token : verifiedTokens) {
                if (token.getTokenId() != null && token.getTokenId().equals(tokens.get(i).getTokenId())) {
                    tokens.get(i).setName(token.getName());
                    tokens.get(i).setIcon(token.getIcon());
                }
            }
        }

        mData.clear();
        mData.addAll(tokens);
        final NodeAPI nodeApi = RetrofitHelper.getInstance().getNodeAPI();

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
                                TokenBalanceBean balance = JSON.parseObject(resp.getData(), TokenBalanceBean.class);
                                for (int j = 0; j < mData.size(); j++) {
                                    if (mData.get(j).getTokenId().equals(balance.getTokenId())) {
                                        mData.get(j).setBalance(balance.getBalance());
                                        mData.get(j).setUnity(balance.getUnity());
                                    }
                                }

                                SPUtils.setString(key, JSON.toJSONString(mData));
                                handleDataChange();
                            }
                        }
                    }, BaseErrorConsumer.create(new BaseErrorConsumer.Callback() {
                        @Override
                        public void onError(int code, String msg) {
                            Log.e(TAG, msg);
                        }
                    }));
        }
        handleDataChange();
    }

    private void updateTokensInfo(){
        final String key = Constants.WATCHED_TOKEN.concat(mAccount.getPublicKey());
        Disposable d = Observable.fromIterable(mData)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(new Function<Token, ObservableSource<Token>>() {
                    @Override
                    public ObservableSource<Token> apply(Token token) throws Exception {
                        return updateTokenInfo(token.getTokenId());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Token>() {
                    @Override
                    public synchronized void accept(Token token) throws Exception {
                        if(token != null) {
                            List<Token> tokens = JSON.parseArray(SPUtils.getString(key), Token.class);
                            if (tokens == null) {
                                tokens = new ArrayList<>();
                            }
                            for (int i=0;i < tokens.size();i++) {
                                if (tokens.get(i).getTokenId().equals(token.getTokenId())){
                                    tokens.get(i).setUnity(token.getUnity());
                                    tokens.get(i).setIssuer(token.getIssuer());
                                    tokens.get(i).setMaker(token.getMaker());
                                }
                            }
                            SPUtils.setString(key, JSON.toJSONString(tokens));

                            for (int i=0;i < mData.size();i++){
                                if (mData.get(i).getTokenId().equals(token.getTokenId())){
                                    mData.get(i).setUnity(token.getUnity());
                                    mData.get(i).setIssuer(token.getIssuer());
                                    mData.get(i).setMaker(token.getMaker());
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }, BaseErrorConsumer.create(new BaseErrorConsumer.Callback() {
                    @Override
                    public void onError(int code, String msg) {
                        ToastUtil.showToast(msg);
                    }
                }));

    }

    private Observable<Token> updateTokenInfo(final String tokenId){
        final Token newToken = new Token();
        final NodeAPI nodeApi = RetrofitHelper.getInstance().getNodeAPI();
        return nodeApi.tokenInfo(tokenId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .concatMap(new Function<RespBean, Observable<RespBean>>() {// request contract info
                    @Override
                    public Observable<RespBean> apply(final RespBean respBean) throws Exception {

                        if(respBean != null) {
                            final TokenBean token = JSON.parseObject(respBean.getData(), TokenBean.class);
                            newToken.setTokenId(token.getTokenId());
                            newToken.setContractId(token.getContractId());
                            newToken.setUnity(token.getUnity());
                            newToken.setMax(token.getMax());
                            newToken.setDescription(TxUtil.decodeAttachment(token.getDescription()).substring(2));
                            return nodeApi.contractInfo(Vsys.tokenId2ContractId(tokenId));
                        }else{
                            return Observable.create(new ObservableOnSubscribe<RespBean>() {
                                @Override
                                public void subscribe(ObservableEmitter<RespBean> emitter) throws Exception {
                                    emitter.onError(new Throwable(respBean.getMsg()));
                                }
                            });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .concatMap(new Function<RespBean, Observable<Token>>() {// contract content
                    @Override
                    public Observable<Token> apply(final RespBean respBean) throws Exception {
                        if(respBean != null) {
                            ContractBean contractBean = JSON.parseObject(respBean.getData(), ContractBean.class);
                            for (ContractInfoBean info: contractBean.getInfo()){
                                if (info.getName().equals("issuer")){
                                    newToken.setIssuer(info.getData());
                                }else if(info.getName().equals("maker")){
                                    newToken.setMaker(info.getData());
                                }
                            }
                            return Observable.create(new ObservableOnSubscribe<Token>() {
                                @Override
                                public void subscribe(ObservableEmitter<Token> emitter) throws Exception {
                                    emitter.onNext(newToken);
                                }
                            });
                        }else{
                            return null;
                        }
                    }
                });
    }

    private void handleDataChange() {
        if(mAdapter != null){
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

    private void setHeaderData() {
        if (mAccount != null) {
            mHeaderBinding.tvBalance.setText(CoinUtil.formatWithUnit(mAccount.getAvailable()));
        }
    }
}
