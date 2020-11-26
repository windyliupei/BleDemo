package win.lioil.bluetooth.ble;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

import win.lioil.bluetooth.PackageRegister;

public class TaskCommunicator {

    private Timer timer = new Timer();
    private TimerTask task;

    private TaskCommunicator(){

        task = new TimerTask() {
            @Override
            public void run() {
                PackageRegister.getInstance().timeout();
                stopTimer();
            }
        };
    }

    public void startTimer() {
        if (this.timer==null){
            this.timer = new Timer();
        }else{
            this.timer.purge();
        }

        if (this.task==null){
            task = new TimerTask() {
                @Override
                public void run() {
                    PackageRegister.getInstance().timeout();
                    stopTimer();
                }};
        }

        this.timer.schedule(task,1000*10);

    }

    public void stopTimer(){
        if (this.timer!=null){

            this.timer.cancel();
            this.timer=null;

            this.task.cancel();
            this.task=null;
        }
    }

    //静态内部类实现线程安全的单例模式
    private static class TaskCommunicatorHolder{
        static TaskCommunicator instance = new TaskCommunicator();
    }
    public static TaskCommunicator getInstance(){
        return TaskCommunicatorHolder.instance;
    }



}
