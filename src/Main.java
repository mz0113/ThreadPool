import java.util.ArrayList;
import java.util.concurrent.*;


/**
 *
 * 线程池中的线程异常无法被 unCaughtException 类捕获。
 *
 * 线程池的创建不要使用Executors，根据阿里的创建规范要使用ThreadPoolExecutor去创建
 *  如果执行了线程池的prestartAllCoreThreads()方法，线程池会提前创建并启动所有核心线程。
 *
 * submit提交规则：
 *  1.提交新的任务后，先检查是否超过了核心池的容量，如果没有，则直接启动线程执行。如果超过了，则提交到阻塞队列中。此时线程池的激活容量还是核心池容量，只有当阻塞队列也满了，才尝试提交到线程池中并与最大线程池容量比较。如果足够，则新启线程直接运行，此时激活容量等于核心池容量+1。
 *  2.如果activeCount = maximumPoolSize，并且阻塞队列也满，则执行拒绝策略。
 *
 *  --> 线程池核心池 --> 阻塞队列 -->线程池最大池 -->拒绝策略
 *  重点在于：任务与核心池相比后，会添加到阻塞队列中而非以最大线程池容量进行
 *
 * corePoolSize：核心线程池线程数量，也即最低要求线程池保证的线程数量。不一定是一开始就直接创建好这样多数量的线程以供备用，可以通过不断增加的任务逐步达到该数量
 * maximumPoolSize:线程池中最大同时执行线程的数量
 * keepAliveTime:表示线程没有任务时最多保持多久然后停止。默认情况下，只有线程池中线程数大于corePoolSize 时，keepAliveTime才会起作用。换句话说，当线程池中的线程数大于corePoolSize，并且一个线程空闲时间达到了keepAliveTime，那么就是shutdown。
 *
 * 1）有界任务队列ArrayBlockingQueue：基于数组的先进先出队列，此队列创建时必须指定大小；
 * 2）无界任务队列LinkedBlockingQueue：基于链表的先进先出队列，如果创建时没有指定此队列大小，则默认为Integer.MAX_VALU E；
 * 3）直接提交队列synchronousQueue：这个队列比较特殊，它不会保存提交的任务，而是将直接新建一个线程来执行新来的任务。
 *
 * 拒绝策略：
 * 1.AbortPolicy:丢弃任务并抛出RejectedExecutionException
 * 2.CallerRunsPolicy：只要线程池未关闭，该策略直接在调用者线程中，运行当前被丢弃的任务。显然这样做不会真的丢弃任务，但是，任
 * 3.务提交线程的性能极有可能会急剧下降。
 * 4.DiscardOldestPolicy：丢弃队列中最老的一个请求，也就是即将被执行的一个任务，并尝试再次提交当前任务。
 * 5.DiscardPolicy：丢弃任务，不做任何处理。
 *
 * 线程数量规范：
 * NCPU = CPU的数量
 * UCPU = 期望对CPU的使用率 0 ≤ UCPU ≤ 1
 * W/C = 等待时间与计算时间的比率
 * 如果希望处理器达到理想的使用率，那么线程池的最优大小为：
 * 线程池大小=NCPU *UCPU(1+W/C)
 *
 *
 * 四种方式弊端：
 * 1）newFixedThreadPool和newSingleThreadExecutor: 主要问题是堆积的请求处理队列可能会耗费非常大的内存，甚至OOM。
 * 2）newCachedThreadPool和newScheduledThreadPool: 主要问题是线程数最大数是Integer.MAX_VALUE，可能会创建数量非常多的线程，甚至OOM。
 *
 * 线程池的关闭：
 * shutdown()：不会立即终止线程池，而是要等所有任务缓存队列中的任务都执行完后才终止，但再也不会接受新的任务
 * shutdownNow()：立即终止线程池，并尝试打断正在执行的任务，并且清空任务缓存队列，返回尚未执行的任务
 *
 *
 * 1.fixedThreadPool
 *      固定大小的线程池，可以指定线程池的大小，该线程池corePoolSize和maximumPoolSize相等，阻塞队列使用的是LinkedBlockingQu eue，大小为整数最大值。
 * 该线程池中的线程数量始终不变，当有新任务提交时，线程池中有空闲线程则会立即执行，如果没有，则会暂存到阻塞队列。对于固定大 小的线程池，不存在线程数量的变化。同时使用无界的LinkedBlockingQueue来存放执行的任务。当任务提交十分频繁的时候，Linked BlockingQueue
 * 迅速增大，存在着耗尽系统资源的问题。而且在线程池空闲时，即线程池中没有可运行任务时，它也不会释放工作线程，还会占用一定的 系统资源，需要shutdown。
 *
 * 2.singleThreadPool
 *      单个线程线程池，只有一个线程的线程池，阻塞队列使用的是LinkedBlockingQueue,若有多余的任务提交到线程池中，则会被暂存到阻 塞队列，待空闲时再去执行。按照先入先出的顺序执行任务。
 *
 * 3.cachedThreadPool 不需要shutDown
 *      缓存线程池，缓存的线程默认存活60秒。线程的核心池corePoolSize大小为0，核心池最大为Integer.MAX_VALUE,阻塞队列使用的是 SynchronousQueue。是一个直接提交的阻塞队列， 他总会迫使线程池增加新的线程去执行新的任务。
 * 在没有任务执行时，当线程的 空闲时间超过keepAliveTime（60秒），则工作线程将会终止被回收，当提交新任务时，如果没有空闲线程，则创建新线程执行任务，会 导致一定的系统开销。
 * 如果同时又大量任务被提交，而且任务执行的时间不是特别快，那么线程池便会新增出等量的线程池处理任务，这 很可能会很快耗尽系统的资源。
 *
 * 4.scheduledThreadPool
 *      scheduleAtFixedRate:是以固定的频率去执行任务，周期是指每次执行任务成功执行之间的间隔。
 *      schedultWithFixedDelay:是以固定的延时去执行任务，延时是指上一次执行成功之后和下一次开始执行的之前的时间。
 */
public class Main {

    public static void main(String[] args) {

        ArrayList<Test> testArrayList = new ArrayList<>(2);
        ThreadPoolExecutor  executor = new ThreadPoolExecutor(300,1100,3, TimeUnit.SECONDS,new ArrayBlockingQueue<>(10));
        for (int i = 0; i < 1000; i++) {
            testArrayList.add(new Test());
        }
        for (Test t:testArrayList) {
            ((ThreadPoolExecutor) executor).submit(t);
           // System.out.println("输出");
        }
        System.out.println( ((ThreadPoolExecutor) executor).getQueue().size());
        System.out.println(((ThreadPoolExecutor) executor).getActiveCount());
        try {
            Thread.sleep(5500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(((ThreadPoolExecutor) executor).getQueue().size());
        System.out.println(((ThreadPoolExecutor) executor).getActiveCount());
    }
}