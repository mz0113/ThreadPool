package LockSupport工具类;

import java.util.concurrent.locks.LockSupport;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("开始运行");
                LockSupport.park(this);
                System.out.println("阻塞结束");
            }
        });
        //thread.join();
        LockSupport.unpark(thread);
        thread.start();
    }
}
