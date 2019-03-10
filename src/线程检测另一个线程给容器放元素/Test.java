package 线程检测另一个线程给容器放元素;

import java.util.ArrayList;
import java.util.List;


/**
 * 第0个
 * 第1个
 * 第2个
 * 第3个
 * 结束
 * 第4个  结束可能出现在第三个之后，也可能是第四个之后。不准反正是
 * 第5个
 * 第6个
 * 第7个
 * 第8个
 * 第9个
 */
public class Test {
    volatile List list = new ArrayList();
    public void add(Object o){
        list.add(o);
    }
    public int size(){
        return list.size();
    }

    public static void main(String[] args) {
        Test t = new Test();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                t.add(new Object());
                System.out.println(String.format("第%d个",i));
            }
        }).start();

       new Thread(() -> {
            while (true){
                if (t.size()==5) {
                    break;
                }else{
                    try {
                        Thread.sleep(300); //如果这样 == 5，直接就错过了。再也不会停止了
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("结束");
        }).start();
    }
}
