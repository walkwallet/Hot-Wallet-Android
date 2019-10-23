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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import systems.v.wallet.data.bean.ContractBean;
import systems.v.wallet.data.bean.ContractContentBean;
import systems.v.wallet.data.bean.ContractInfoBean;
import systems.v.wallet.data.bean.ErrorBean;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.bean.TokenBean;
import systems.v.wallet.data.statics.TokenHelper;
import systems.v.wallet.databinding.ActivityAddTokenBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.contract.adapter.AddTokenAdapter;
import systems.v.wallet.ui.view.contract.adapter.TokenAdapter;
import systems.v.wallet.ui.view.transaction.ScannerActivity;
import systems.v.wallet.utils.AssetJsonUtil;
import systems.v.wallet.utils.Constants;
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

                addWatchedToken(token.getTokenId());
            }
        });
        initTokenData();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_add:
                addWatchedToken(mBinding.etTokenId.getText().toString());
                break;
            case R.id.btn_paste: {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = cm.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    ClipData.Item item = clipData.getItemAt(0);
                    String text = item.getText().toString();
                    mBinding.etTokenId.setText(text);
                }
            }
                break;
            case R.id.btn_scan:
                ScannerActivity.launch(this);
                break;
        }
    }

    public void addWatchedToken(final String tokenId){
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


        Disposable d = Observable.create(new ObservableOnSubscribe<List<Token>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<Token>> emitter) throws Exception {
                        if(mData == null) {
                            mData = new ArrayList<>();
                        }
                        mData.clear();

                        List<Token> verifiedTokens = TokenHelper.getVerifiedFromCache(AddTokenActivity.this, mAccount.getNetwork());
                        for (int i = 0; i < verifiedTokens.size(); i++) {
                            Token vToken = verifiedTokens.get(i);
                            if (tokens != null) {
                                for (Token token : tokens) {
                                    if (token.getTokenId().equals(verifiedTokens.get(i).getTokenId())) {
                                        vToken.setAdded(true);
                                        break;
                                    }
                                }
                            }
                            mData.add(vToken);
                        }

                        emitter.onNext(mData);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Token>>() {
                    @Override
                    public void accept(List<Token> list) throws Exception {
                        if(mBinding.rcvTokens.getAdapter() != null) {
                            mBinding.rcvTokens.getAdapter().notifyDataSetChanged();
                        }
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
            //TODO refresh
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}