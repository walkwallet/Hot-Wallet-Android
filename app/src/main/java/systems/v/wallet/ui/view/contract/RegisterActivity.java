package systems.v.wallet.ui.view.contract;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.R;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.bean.AccountBean;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.bean.TokenBean;
import systems.v.wallet.ui.BaseThemedActivity;

public class RegisterActivity extends BaseThemedActivity {

    public static void launch(Activity from, String publicKey) {
        Intent intent = new Intent(from, RegisterActivity.class);
        intent.putExtra("publicKey", publicKey);
        from.startActivity(intent);
    }

    private final String TAG = RegisterActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getTokenBalance();
    }

    private void getTokenBalance() {
        Disposable d = RetrofitHelper.getInstance().getNodeAPI().tokenInfo( "2GaWFxW4Tc13a6gyZVYDbNTAspFgzzTHWvvqXLr3H")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RespBean>() {
                    @Override
                    public void accept(RespBean respBean) throws Exception {
                        Log.d(TAG, JSON.toJSONString(respBean));
                        TokenBean tokenBean = JSON.parseObject(respBean.getData(), TokenBean.class);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.v("derror", throwable.getMessage());
                    }
                });
    }
}
