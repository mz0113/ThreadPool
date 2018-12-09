public class Test extends Thread{
    @Override
    public void run(){
        this.setName("自定义线程");
        try {
            Thread.sleep(90000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
