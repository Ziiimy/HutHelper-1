package cn.nicolite.huthelper.view.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tencent.android.tpush.XGPushManager;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import butterknife.BindView;
import butterknife.OnClick;
import cn.nicolite.huthelper.R;
import cn.nicolite.huthelper.base.activity.BaseActivity;
import cn.nicolite.huthelper.model.Constants;
import cn.nicolite.huthelper.model.bean.User;
import cn.nicolite.huthelper.presenter.UserInfoPresenter;
import cn.nicolite.huthelper.utils.DensityUtils;
import cn.nicolite.huthelper.view.iview.IUserInfoView;
import io.rong.imkit.RongIM;

/**
 * 用户信息界面
 * Created by nicolite on 17-10-28.
 */

public class UserInfoActivity extends BaseActivity implements IUserInfoView{
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.iv_user_headview)
    ImageView ivUserHeadview;
    @BindView(R.id.rl_user_headview)
    RelativeLayout rlUserHeadview;
    @BindView(R.id.tv_user_nickname)
    TextView tvUserNickname;
    @BindView(R.id.tv_user_password)
    TextView tvUserPassword;
    @BindView(R.id.tv_user_name)
    TextView tvUserName;
    @BindView(R.id.iv_user_gender)
    ImageView ivUserGender;
    @BindView(R.id.tv_user_num)
    TextView tvUserNum;
    @BindView(R.id.tv_user_school)
    TextView tvUserSchool;
    @BindView(R.id.tv_user_class)
    TextView tvUserClass;
    private UserInfoPresenter userInfoPresenter;
    private final int REQUEST_CODE_CHOOSE = 111;
    private final int REQUEST_CODE_CUT = 222;
    @Override
    protected void initConfig(Bundle savedInstanceState) {
        setDeepColorStatusBar(true);
        setImmersiveStatusBar(true);
        setSlideExit(true);
    }

    @Override
    protected void initBundleData(Bundle bundle) {

    }

    @Override
    protected int setLayoutId() {
        return R.layout.activity_user_info;
    }

    @Override
    protected void doBusiness() {
        toolbarTitle.setText("个人信息");
        userInfoPresenter = new UserInfoPresenter(this, this);
        userInfoPresenter.showUserData();
    }

    @OnClick({R.id.toolbar_back, R.id.rl_user_headview, R.id.rl_user_nickname, R.id.rl_user_password, R.id.user_logout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.toolbar_back:
                finish();
                break;
            case R.id.rl_user_headview:
                userInfoPresenter.changAvatar();
                break;
            case R.id.rl_user_nickname:
                break;
            case R.id.rl_user_password:
                break;
            case R.id.user_logout:
                User user = boxHelper.getUserBox().get(1);
                if (user == null){
                    return;
                }
                RongIM.getInstance().logout();
                XGPushManager.deleteTag(context, user.getStudentKH());
                XGPushManager.registerPush(context, "*");
                XGPushManager.unregisterPush(context);
                boxHelper.getUserBox().removeAll();
                boxHelper.getConfigureBox().removeAll();
                startActivity(LoginActivity.class);
                finish();
                break;
        }
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void closeLoading() {

    }

    @Override
    public void showMessage(String msg) {

    }

    @Override
    public void showUserInfo(User user) {
        tvUserNickname.setText(user.getUsername());
        tvUserName.setText(user.getTrueName());
        ivUserGender.setImageResource(user.getSex().equals("男")? R.drawable.male : R.drawable.female);
        tvUserSchool.setText(user.getDep_name());
        tvUserNum.setText(user.getStudentKH());
        tvUserClass.setText(user.getClass_name());

        if (!TextUtils.isEmpty(user.getHead_pic_thumb())){
            int width = DensityUtils.dp2px(context, 40);
            Glide
                    .with(this)
                    .load(Constants.PICTURE_URL + user.getHead_pic_thumb())
                    .override(width, width)
                    .skipMemoryCache(true)
                    .dontAnimate()
                    .into(ivUserHeadview);
        }
    }

    @Override
    public void changeAvatarSuccess(Bitmap bitmap) {
        ivUserHeadview.setImageBitmap(bitmap);
    }

    @Override
    public void changeAvatar() {
        Matisse.from(this)
                .choose(MimeType.of(MimeType.JPEG, MimeType.PNG))
                .countable(true)
                .maxSelectable(1)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(REQUEST_CODE_CHOOSE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
