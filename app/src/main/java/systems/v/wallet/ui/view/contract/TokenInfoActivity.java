package systems.v.wallet.ui.view.contract;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.schedulers.ComputationScheduler;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.R;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.api.NodeAPI;
import systems.v.wallet.data.bean.ContractBean;
import systems.v.wallet.data.bean.ContractInfoBean;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.bean.TokenBean;
import systems.v.wallet.databinding.ActivityTokenInfoBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.SPUtils;
import systems.v.wallet.utils.UIUtil;
import vsys.Vsys;

public class TokenInfoActivity extends BaseThemedActivity {
    private Token mToken;

    public static void launch(Activity from, String publicKey, Token token) {
        Intent intent = new Intent(from, TokenInfoActivity.class);
        intent.putExtra("publicKey", publicKey);
        intent.putExtra("token", JSON.toJSONString(token));
        from.startActivity(intent);
    }

    private ActivityTokenInfoBinding mBinding;
    private final String TAG = TokenInfoActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToken = JSON.parseObject(getIntent().getStringExtra("token"), Token.class);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_token_info);
        initData();
    }

    private void initView(){
        setAppBar(mBinding.toolbar);
        UIUtil.addTokenDetail(getLayoutInflater(), mBinding.llContainer, mToken);
    }

    private void initData(){
        final NodeAPI nodeApi = RetrofitHelper.getInstance().getNodeAPI();
        final String key = Constants.WATCHED_TOKEN.concat(mAccount.getPublicKey());
        Disposable d = nodeApi.tokenInfo(mToken.getTokenId())
                .compose(this.<RespBean>bindLoading())
                .flatMap(new Function<RespBean, Observable<RespBean>>() {
                    @Override
                    public Observable<RespBean> apply(RespBean respBean) throws Exception {
                        TokenBean token = JSON.parseObject(respBean.getData(), TokenBean.class);
                        mToken.setIssuedAmount(token.getTotal());
                        return nodeApi.contractInfo(Vsys.tokenId2ContractId(mToken.getTokenId()));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RespBean>() {
                    @Override
                    public void accept(RespBean respBean) throws Exception {
                        Log.d(TAG, JSON.toJSONString(respBean));
                        ContractBean token = JSON.parseObject(respBean.getData(), ContractBean.class);
                        for (ContractInfoBean info: token.getInfo()){
                            if (info.getName().equals("issuer")){
                                mToken.setIssuer(info.getData());
                            }else if(info.getName().equals("maker")){
                                mToken.setMaker(info.getData());
                            }
                        }
                        List<Token> tokens = JSON.parseArray(SPUtils.getString(key), Token.class);
                        if (tokens != null) {
                            for (int i=0;i < tokens.size();i++){
                                if(tokens.get(i).getTokenId().equals(mToken.getTokenId())){
                                    tokens.set(i, mToken);
                                }
                            }

                            SPUtils.setString(key, JSON.toJSONString(tokens));
                        }
                        initView();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, throwable.getMessage());
                    }
                });
    }
}
