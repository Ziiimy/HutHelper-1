package cn.nicolite.huthelper.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatCrashReporter;
import com.tencent.stat.StatService;
import com.tencent.stat.common.StatConstants;

import java.util.List;

import cn.nicolite.huthelper.BuildConfig;
import cn.nicolite.huthelper.db.DaoHelper;
import cn.nicolite.huthelper.db.dao.DaoSession;
import cn.nicolite.huthelper.db.dao.UserDao;
import cn.nicolite.huthelper.model.Constants;
import cn.nicolite.huthelper.model.bean.User;
import cn.nicolite.huthelper.utils.ListUtils;
import cn.nicolite.huthelper.view.activity.MainActivity;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;
import io.rong.push.RongPushClient;


/**
 * Created by nicolite on 17-9-5.
 */

public class MApplication extends Application {
    public static Context AppContext;
    public static MApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        AppContext = getApplicationContext();
        application = this;

        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this);
        }

        //注册融云小米push
        RongPushClient.registerMiPush(this, Constants.XIAOMI_APPID, Constants.XIAOMI_APPKEY);

        //初始化融云
        RongIM.init(getApplicationContext());

        //设置支持消息回执的会话类型
        Conversation.ConversationType[] types = new Conversation.ConversationType[]{
                Conversation.ConversationType.PRIVATE,
                Conversation.ConversationType.GROUP,
                Conversation.ConversationType.DISCUSSION
        };

        RongIM.getInstance().setReadReceiptConversationTypeList(types);

        try {
            StatConfig.init(this);

            String loginUser = getLoginUser();
            if (!TextUtils.isEmpty(loginUser)) {
                List<User> list = getDaoSession().getUserDao().queryBuilder()
                        .where(UserDao.Properties.User_id.eq(loginUser))
                        .list();
                if (!ListUtils.isEmpty(list)) {
                    String studentKH = list.get(0).getStudentKH();
                    if (!TextUtils.isEmpty(studentKH)) {
                        StatConfig.setCustomUserId(this, studentKH);
                    }
                }
            }

            StatService.startStatService(this, Constants.MAT_APPKEY, StatConstants.VERSION);
            //开启Java Crash异常捕获
            StatCrashReporter.getStatCrashReporter(this).setJavaCrashHandlerStatus(true);
            //开启Native异常捕获
            // StatCrashReporter.getStatCrashReporter(getApplicationContext()).setJniNativeCrashStatus(true);
        } catch (MtaSDkException e) {
            e.printStackTrace();
        }

        //只在MainActivity上显示升级对话框
        Beta.canShowUpgradeActs.add(MainActivity.class);
        //初始化腾讯Bugly
        Bugly.init(this, Constants.BUGLY_APPID, BuildConfig.LOG_DEBUG);
        Bugly.setIsDevelopmentDevice(this, BuildConfig.LOG_DEBUG);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //初始化多dex配置
        MultiDex.install(this);
        Beta.installTinker();
    }

    /**
     * 获取当前登录用户
     */
    protected String getLoginUser() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("login_user", Context.MODE_PRIVATE);
        return preferences.getString("userId", null);
    }

    /**
     * 获取daoSession
     */
    protected DaoSession getDaoSession() {
        return DaoHelper.getDaoHelper(getApplicationContext()).getDaoSession();
    }

}
