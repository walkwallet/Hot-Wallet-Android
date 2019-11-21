package systems.v.wallet.ui.view.contract;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.reactivestreams.Subscriber;

import go.error;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.Base58;
import systems.v.wallet.basic.utils.TxUtil;
import systems.v.wallet.basic.wallet.ContractFunc;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.data.BaseErrorConsumer;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.api.NodeAPI;
import systems.v.wallet.data.api.PublicApi;
import systems.v.wallet.data.bean.ContractBean;
import systems.v.wallet.data.bean.ContractContentBean;
import systems.v.wallet.data.bean.ContractInfoBean;
import systems.v.wallet.data.bean.ErrorBean;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.bean.TokenBean;
import systems.v.wallet.data.bean.publicApi.ListRespBean;
import systems.v.wallet.data.bean.publicApi.TokenInfoBean;
import systems.v.wallet.data.statics.TokenHelper;
import systems.v.wallet.databinding.ActivityAddTokenBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.contract.adapter.AddTokenAdapter;
import systems.v.wallet.ui.view.contract.adapter.TokenAdapter;
import systems.v.wallet.ui.view.transaction.ScannerActivity;
import systems.v.wallet.utils.AssetJsonUtil;
import systems.v.wallet.utils.ClipUtil;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.LogUtil;
import systems.v.wallet.utils.SPUtils;
import systems.v.wallet.utils.ToastUtil;
import systems.v.wallet.utils.bus.AppBus;
import systems.v.wallet.utils.bus.AppEvent;
import systems.v.wallet.utils.bus.AppEventType;
import vsys.Vsys;

public class AddTokenActivity extends BaseThemedActivity implements View.OnClickListener{

    public static void launch(Activity from, String publicKey) {
        Intent intent = new Intent(from, AddTokenActivity.class);
        intent.putExtra("publicKey", publicKey);
        from.startActivity(intent);
    }

    private final String TAG = AddTokenActivity.class.getSimpleName();
    private ActivityAddTokenBinding mBinding;
    private List<Token> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_token);
        mBinding.setClick(this);
        setAppBar(mBinding.toolbar);
        mBinding.rcvTokens.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rcvTokens.setAdapter(new AddTokenAdapter(mData, AddTokenActivity.this){
            @Override
            public void addToken(Token token) {

                addWatchedToken(token.getName(), token.getTokenId(), true);
            }
        });
        initTokenData();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_add:
                List<Token> tokens = TokenHelper.getVerifiedFromCache(this, mAccount.getNetwork());
                String name = null;
                String tokenId = mBinding.etTokenId.getText().toString();
                boolean verified = false;
                if(tokens != null) {
                    for (Token token : tokens) {
                        if (token.getTokenId().equals(tokenId) ||
                                (token.getName() + " Token").toLowerCase().equals(tokenId.toLowerCase()) ||
                                token.getName().toLowerCase().equals(tokenId.toLowerCase())){
                            name = token.getName();
                            tokenId = token.getTokenId();
                            verified = true;
                            break;
                        }
                    }
                }
                addWatchedToken(name, tokenId, verified);
                break;
            case R.id.btn_paste: {
                mBinding.etTokenId.setText(ClipUtil.getClip(this));
            }
                break;
            case R.id.btn_scan:
                ScannerActivity.launch(this);
                break;
        }
    }

    public void addWatchedToken(final String name, final String tokenId, boolean isVerified){
        final String key = Constants.WATCHED_TOKEN.concat(mAccount.getPublicKey());
        if(tokenId.isEmpty()){
            return ;
        }
        final List<Token> tokens = JSON.parseArray(SPUtils.getString(key), Token.class);
        if(tokens != null){
            for (Token t : tokens) {
                if (tokenId.equals(t.getTokenId())) {
                    ToastUtil.showToast(R.string.add_token_added_token);
                    return;
                }
            }
        }
        final NodeAPI nodeApi = RetrofitHelper.getInstance().getNodeAPI();
        final Token newToken = new Token();
        newToken.setVerified(isVerified);
        Disposable d = nodeApi.tokenInfo(tokenId)// request token info
                .compose(this.<RespBean>bindLoading())
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
                            if (name != null && !name.isEmpty()) {
                                newToken.setName(name);
                            }
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
                .concatMap(new Function<RespBean, Observable<RespBean>>() {// contract content
                    @Override
                    public Observable<RespBean> apply(final RespBean respBean) throws Exception {
                        if(respBean != null) {
                            ContractBean contractBean = JSON.parseObject(respBean.getData(), ContractBean.class);
                            for (ContractInfoBean info: contractBean.getInfo()){
                                if (info.getName().equals("issuer")){
                                    newToken.setIssuer(info.getData());
                                }else if(info.getName().equals("maker")){
                                    newToken.setMaker(info.getData());
                                }
                            }
                            return nodeApi.contractContent(Vsys.tokenId2ContractId(tokenId));
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
                .concatMap(new Function<RespBean, Observable<String>>() {// save token
                    @Override
                    public Observable<String> apply(final RespBean respBean) throws Exception {
                        if(respBean != null && respBean.getCode() == 0) {
                            return Observable.create(new ObservableOnSubscribe<String>() {
                                @Override
                                public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                                    if (respBean.getCode() == 0) {
                                        List<Token> tokens = JSON.parseArray(SPUtils.getString(key), Token.class);
                                        if (tokens == null) {
                                            tokens = new ArrayList<>();
                                        }
                                        ContractContentBean contract = JSON.parseObject(respBean.getData(), ContractContentBean.class);
                                        List<ContractFunc> funcs = new ArrayList<>((JSON.parseArray(Vsys.decodeContractTextrue(contract.getTextual().getDescriptors()), ContractFunc.class)));
                                        for (ContractFunc func : funcs){
                                            if (func.getName().equals("split")){
                                                newToken.setSpilt(true);
                                            }
                                        }
                                        newToken.setFuncs(funcs.toArray(new ContractFunc[funcs.size()]));
                                        tokens.add(newToken);
                                        SPUtils.setString(key, JSON.toJSONString(tokens));
                                        emitter.onNext("");
                                    } else {
                                        emitter.onNext(respBean.getMsg());
                                    }
                                }
                            });
                        }else{
                            return Observable.create(new ObservableOnSubscribe<String>() {
                                @Override
                                public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                                    emitter.onError(new Throwable(respBean.getMsg()));
                                }
                            });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String result) throws Exception {
                        mBinding.tvTokenError.setVisibility(View.GONE);
                        if (result.isEmpty()){
                            AppBus.getInstance().post(new AppEvent(AppEventType.TOKEN_ADD));
                            ToastUtil.showToast(R.string.add_token_success);
                            finish();
                        } else{
                            ToastUtil.showToast("Accept result msg" + result);
                        }
                    }
                }, BaseErrorConsumer.create(new BaseErrorConsumer.Callback() {
                    @Override
                    public void onError(int code, String msg) {
                        mBinding.tvTokenError.setVisibility(View.VISIBLE);
                        mBinding.tvTokenError.setText(msg);
                    }
                }));
    }

    private void initTokenData(){
        final String key = Constants.WATCHED_TOKEN.concat(mAccount.getPublicKey());
        final List<Token> tokens = JSON.parseArray(SPUtils.getString(key), Token.class);
        List<Token> verifiedTokenCache = TokenHelper.getVerifiedFromCache(this, mAccount.getNetwork());
        if(mData == null) {
            mData = new ArrayList<>();
        }
        mData.clear();

        for (int i = 0; i < verifiedTokenCache.size(); i++) {
            Token vToken = verifiedTokenCache.get(i);
            boolean isAdded = false;
            if (tokens != null) {
                for (int j = 0; j < tokens.size(); j++) {
                    if (tokens.get(j).getTokenId().equals(vToken.getTokenId())) {
                        isAdded = true;
                        break;
                    }
                }
            }
            vToken.setAdded(isAdded);
            mData.add(vToken);
        }

        Collections.sort(mData, new Comparator<Token>() {
            @Override
            public int compare(Token o1, Token o2) {
                if (o1.isAdded() && !o2.isAdded()){
                    return -1;
                }else if(!o1.isAdded() && o2.isAdded()){
                    return 1;
                }else{
                    return 0;
                }
            }
        });

        updateVerifiedToken();
    }

    private void updateVerifiedToken(){
        final String key = Constants.WATCHED_TOKEN.concat(mAccount.getPublicKey());
        final List<Token> tokens = JSON.parseArray(SPUtils.getString(key), Token.class);
        final PublicApi publicApi = RetrofitHelper.getInstance().getPublicAPI();
        Disposable d1 = publicApi.getTokenList()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .concatMap(new Function<systems.v.wallet.data.bean.publicApi.RespBean, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(final systems.v.wallet.data.bean.publicApi.RespBean respBean) throws Exception {
                        if(respBean != null && respBean.getCode() == 0) {
                            ListRespBean tokenList = JSON.parseObject((String)respBean.getData(), ListRespBean.class);

                            List<TokenInfoBean> verifiedTokens = new ArrayList<>();
                            for (Object o : tokenList.getList()){
                                JSONObject jo = (JSONObject) o;
                                verifiedTokens.add(JSONObject.parseObject(jo.toJSONString(), TokenInfoBean.class));
                            }

                            if(mData == null) {
                                mData = new ArrayList<>();
                            }
                            mData.clear();

                            for (int i = 0; i < verifiedTokens.size(); i++) {
                                TokenInfoBean vToken = verifiedTokens.get(i);
                                boolean isAdded = false;
                                if (tokens != null) {
                                    for (int j = 0; j < tokens.size(); j++) {
                                        if (tokens.get(j).getTokenId().equals(vToken.getId())) {
                                            isAdded = true;
                                            break;
                                        }
                                    }
                                }
                                Token t = new Token();
                                t.setAdded(isAdded);
                                t.setName(vToken.getName());
                                t.setIcon((mWallet.getNetwork().equals(Vsys.NetworkMainnet) ? Constants.PUBLIC_API_SERVER : Constants.PUBLIC_API_SERVER_TEST ) + vToken.getIconUrl());
                                t.setTokenId(vToken.getId());
                                mData.add(t);
                            }

                            Collections.sort(mData, new Comparator<Token>() {
                                @Override
                                public int compare(Token o1, Token o2) {
                                    if (o1.isAdded() && !o2.isAdded()){
                                        return -1;
                                    }else if(!o1.isAdded() && o2.isAdded()){
                                        return 1;
                                    }else{
                                        return 0;
                                    }
                                }
                            });

                            SPUtils.setString(TokenHelper.VERIFIED_TOKEN_ + mAccount.getNetwork(), JSON.toJSONString(mData.toArray()));

                            return Observable.create(new ObservableOnSubscribe<String>() {
                                @Override
                                public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                                    emitter.onNext("");
                                }
                            });
                        }else{
                            return Observable.create(new ObservableOnSubscribe<String>() {
                                @Override
                                public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                                    if (respBean != null) {
                                        emitter.onNext(respBean.getMsg());
                                    }
                                }
                            });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        if (mBinding.rcvTokens.getAdapter() != null) {
                            mBinding.rcvTokens.getAdapter().notifyDataSetChanged();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String qrContents = result.getContents();
            if (!TextUtils.isEmpty(qrContents)) {
                mBinding.etTokenId.setText(qrContents);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_token, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            updateVerifiedToken();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
