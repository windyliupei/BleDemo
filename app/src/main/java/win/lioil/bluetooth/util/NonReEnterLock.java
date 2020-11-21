package win.lioil.bluetooth.util;

//非可重入锁，保证每个sender一次只能发送一组数据
public class NonReEnterLock {



    private boolean isLocked = false;
    public synchronized void lock() throws InterruptedException{
        while(isLocked){
            wait();
        }
        isLocked = true;
    }


    public synchronized void unlock(){
        isLocked = false;
        notify();
    }

}
