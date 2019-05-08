package 多线程下Integer自增;

public class TEST implements Runnable {
    public static Integer i = 0;
    static TEST instance = new TEST();
    private Object object = new Object();

    @Override
    public void run() {
        synchronized (object) {
            for (int j = 0; j < 100000; j++) {
                try {
                    System.out.println(Thread.currentThread().getName());
                    Thread.sleep(100);
                    Thread.yield();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadGroup group = new ThreadGroup("213");
        
        Thread thread1 = new Thread(group,instance);
        Thread thread2 = new Thread(instance);
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        System.out.println(i);
    }
}
