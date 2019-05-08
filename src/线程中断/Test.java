package 线程中断;

public class Test {
/*    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    try {
                        Thread.sleep(1000);//sleep被中断后会清除掉中断标志
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();//所以如果在sleep时候中断，并且不自己手动再中断一次（这个方法会设置中断标记），就会继续循环下去。因为中断标记已经被清除了
                        if (Thread.interrupted()) {
                            System.out.println("被中断");//interrupted()会判断是否被中断，并清除中断标记位，因此仍然会无限循环下去
                        }
                    }
                    System.out.println("----");
                }
            }
        });
        thread.start();
        Thread.sleep(500);
        thread.interrupt();//中断线程
    }*/

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    System.out.println("----");
                }
            }
        });
        thread.start();
        Thread.sleep(500);
        thread.stop();//中断线程
    }
}
