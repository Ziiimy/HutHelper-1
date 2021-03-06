package cn.nicolite.huthelper.presenter;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.nicolite.huthelper.base.presenter.BasePresenter;
import cn.nicolite.huthelper.model.bean.Configure;
import cn.nicolite.huthelper.model.bean.HttpResult;
import cn.nicolite.huthelper.model.bean.User;
import cn.nicolite.huthelper.network.api.APIUtils;
import cn.nicolite.huthelper.utils.EncryptUtils;
import cn.nicolite.huthelper.utils.ListUtils;
import cn.nicolite.huthelper.utils.LogUtils;
import cn.nicolite.huthelper.view.activity.UserInfoCardActivity;
import cn.nicolite.huthelper.view.iview.IUserInfoCardView;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by nicolite on 17-11-12.
 */

public class UserInfoCardPresenter extends BasePresenter<IUserInfoCardView, UserInfoCardActivity> {
    public UserInfoCardPresenter(IUserInfoCardView view, UserInfoCardActivity activity) {
        super(view, activity);
    }

    public void showInfo(String userId) {
        if (TextUtils.isEmpty(userId)) {
            getView().showMessage("获取用户信息失败！");
            return;
        }
        List<Configure> configureList = getConfigureList();

        if (ListUtils.isEmpty(configureList)) {
            getView().showMessage("获取用户信息失败！");
            return;
        }

        Configure configure = configureList.get(0);
        User user = configure.getUser();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM", Locale.CHINA);
        String env = user.getStudentKH() + configure.getAppRememberCode() + userId
                + simpleDateFormat.format(new Date());
        LogUtils.d(TAG, env);
        APIUtils
                .getUserAPI()
                .getUserInfo(user.getStudentKH(), configure.getAppRememberCode(), userId,
                        EncryptUtils.SHA1(env))
                .compose(getActivity().<HttpResult<User>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HttpResult<User>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (getView() != null) {
                            getView().showLoading();
                        }
                    }

                    @Override
                    public void onNext(HttpResult<User> userHttpResult) {
                        if (getView() != null) {
                            getView().closeLoading();
                            if (userHttpResult.getCode() == 200){
                                getView().showInfo(userHttpResult.getData());
                            }else {
                                getView().showMessage("获取信息失败，" + userHttpResult.getCode());
                            }

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (getView() != null) {
                            getView().closeLoading();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
