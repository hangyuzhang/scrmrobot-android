package com.scrm.robot;

import android.app.Activity;
import android.app.Application;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.DiskLogStrategy;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.scrm.robot.taskmanager.JobSchedulerMessageReceiver;
import com.scrm.robot.taskmanager.RobotAccessibilityContext;
import com.scrm.robot.taskmanager.RobotJobFactory;
import com.scrm.robot.taskmanager.RobotJobScheduler;
import com.scrm.robot.taskmanager.job.BaseRobotJob;
import com.scrm.robot.utils.ApplicationUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.Base64;
import java.util.Date;

public class RobotApplication extends Application {
   private RobotAccessibilityContext robotAccessibilityContext;
   private Date lastEventTime;
//    private WeakReference<WeWorkAccessibilityService> weWorkAccessibilityService;
    private WeWorkAccessibilityService  weWorkAccessibilityService;

    private LocalBroadcastManager localBroadcastManager;
    private static JobSchedulerMessageReceiver receiver;

    private RobotJobScheduler robotJobScheduler;
    private RobotJobFactory robotJobFactory;
    private Activity jobActivity;
    private int jobId=0;
    private String currentWeWorkActivity=null;

    @Override
    public void onCreate() {
        super.onCreate();

        this.localBroadcastManager= LocalBroadcastManager.getInstance(this);
        if(receiver==null) {
            receiver = new JobSchedulerMessageReceiver();
            this.localBroadcastManager.registerReceiver(receiver, new IntentFilter(Constants.JOB_SCHEDULER_MSG_RECEIVER));
        }

        ApplicationUtil.init(this);
        this.initLogger();
        Logger.i("应用创建");
    }

    public RobotAccessibilityContext getRobotAccessibilityContext() {
        return robotAccessibilityContext;
    }

    public void setRobotAccessibilityContext(RobotAccessibilityContext robotAccessibilityContext) {
        this.robotAccessibilityContext = robotAccessibilityContext;
        if(robotAccessibilityContext.getWeWorkActivityClassName()!=null){
            this.currentWeWorkActivity=robotAccessibilityContext.getWeWorkActivityClassName();
        }
        this.lastEventTime=new Date();
    }

    public Date getLastEventTime() {
        return lastEventTime;
    }

    public void setLastEventTime(Date lastEventTime) {
        this.lastEventTime = lastEventTime;
    }

    public String getCurrentWeWorkActivityClassName() {
        return currentWeWorkActivity;
    }

    public void setCurrentWeWorkActivity(String currentWeWorkActivity) {
        this.currentWeWorkActivity = currentWeWorkActivity;
    }

    public  WeWorkAccessibilityService getWeWorkAccessibilityService() {
        return weWorkAccessibilityService;
    }

    public void setWeWorkAccessibilityService(WeWorkAccessibilityService weWorkAccessibilityService) {
        this.weWorkAccessibilityService  =weWorkAccessibilityService;
    }

    public int generateJobId(){
        return jobId+=1;
    }


    public RobotJobScheduler getRobotJobScheduler() {
        return robotJobScheduler;
    }

    public void setRobotJobScheduler(RobotJobScheduler robotJobScheduler) {
        this.robotJobScheduler = robotJobScheduler;
    }

    public Activity getJobActivity() {
        return jobActivity;
    }

    public void setJobActivity(Activity jobActivity) {
        this.jobActivity = jobActivity;
    }

    public RobotJobFactory getRobotJobFactory() {
        return robotJobFactory;
    }

    public void setRobotJobFactory(RobotJobFactory robotJobFactory) {
        this.robotJobFactory = robotJobFactory;
    }

    public LocalBroadcastManager getLocalBroadcastManager() {
        return localBroadcastManager;
    }

    public void setLocalBroadcastManager(LocalBroadcastManager localBroadcastManager) {
        this.localBroadcastManager = localBroadcastManager;
    }

    public BaseRobotJob getCurrentJob(){
        if(this.getRobotJobScheduler()!=null && this.getRobotJobScheduler().getRobotJobExecutor()!=null){
            return this.getRobotJobScheduler().getRobotJobExecutor().getCurrentJob();
        }
        return null;
    }

    private void initLogger(){
        try {
            // region 文件log
            String diskPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String folder = diskPath + File.separatorChar + "smr" + File.separatorChar +"log";
            HandlerThread ht = new HandlerThread("AndroidFileLogger." + folder);
            ht.start();

            Logger.addLogAdapter(new DiskLogAdapter());
            Class<?> clazz = Class.forName("com.orhanobut.logger.DiskLogStrategy$WriteHandler");
            Constructor constructor = clazz.getDeclaredConstructor(Looper.class, String.class, int.class);
            //开启强制访问
            constructor.setAccessible(true);
            Handler handler = (Handler) constructor.newInstance(ht.getLooper(), folder, Constants.MAX_BYTES * 1024);

            //创建磁盘存储策略
            FormatStrategy strategy = CsvFormatStrategy.newBuilder().logStrategy(new DiskLogStrategy(handler)).build();
            DiskLogAdapter diskLogAdapter = new DiskLogAdapter(strategy);
            // endregion


            Logger.addLogAdapter(diskLogAdapter);
            Logger.addLogAdapter(new AndroidLogAdapter());

        }catch (Exception ex){
            ex.printStackTrace();

        }

    }
}
