package com.itheima.netty;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
/**
 * @description
 * @author: ts
 * @create:2021-04-27 15:26
 */
public class JdkFutureTest {

    // private static final Logger log = LoggerFactory.getLogger(JdkFutureTest.class);

    @Test
    public void testFuture() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);

        log.info("-------主线程提交runnable任务-------");
        // 提交runnable类型的任务,任务执行完毕后提交者无法获取执行结果,没有返回
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                log.info("---异步线程---执行任务");
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        log.info("-------主线程继续执行-------");
        log.info("----------------------华丽的分割线-------------------------");

        log.info("-------主线程提交callable任务---第一种写法-------");
        //提交callable任务，是有返回的，提交者可以获取异步线程执行结果
        Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
               log.info("线程池中的异步线程正在执行任务-----");
               TimeUnit.SECONDS.sleep(3);
               log.info("异步线程执行完毕,返回结果");
                return "async task result";
            }
        });
        //提交者阻塞等待获取异步线程执行结果
        try {
            log.info("-------主线程等待callable任务返回结果-------");
            String result = future.get();
            log.info("提交者线程中获取到的异步线程执行结果是:{}",result);
        } catch (ExecutionException e) {
            log.error("提交者阻塞等待异步线程的执行结果异常,{}",e.getMessage());
        }

        log.info("----------------------华丽的分割线-------------------------");

        log.info("-------主线程提交callable任务---第二种写法-------");
        FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                log.info("线程池中的异步线程正在执行任务-----");
                TimeUnit.SECONDS.sleep(3);
                log.info("异步线程执行完毕,返回结果");
                return "async task result2";
            }
        });
        executorService.submit(task);
        //提交者阻塞等待获取异步线程执行结果
        try {
            log.info("-------主线程等待callable任务返回结果-------");
            String result = task.get();
            log.info("提交者线程中获取到的异步线程执行结果是:{}",result);
        } catch (ExecutionException e) {
            log.error("提交者阻塞等待异步线程的执行结果异常,{}",e.getMessage());
        }
    }

    /**
     * 通过callable+future，虽然可以在提交线程中拿到异步线程的执行结果，但它并不是真正的异步，没有实现回调，提交线程中仍然需要通过get()
     * 阻塞等待,所以在Java8 中又新增了一个真正的异步函数：CompletableFuture
     *
     * Java 8 中新增加了一个类：CompletableFuture，它提供了非常强大的 Future 的扩展功能，最重要的是实现了回调的功能
     */

    @Test
    public void testCompletableFuture() throws InterruptedException {
        /**
         * 异步非阻塞,执行无返回值任务,
         * public static CompletableFuture<Void> runAsync(Runnable runnable){..}
         *
         * public static CompletableFuture<Void> runAsync(Runnable runnable,Executor executor){..}
         * 同时支持传入自定义的线程池，如果不传入线程池的话默认是使用 ForkJoinPool.commonPool()作为它的线程池执行异步代码
         *
         */
        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                log.info("---开始异步线程执行任务----");
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("---异步线程执行任务结束----");
            }
        });
        log.info("---主线程----");
        TimeUnit.SECONDS.sleep(5);
    }


    @Test
    public void testCompletableSupplyAsync() throws InterruptedException {
        /**
         * 异步非阻塞，执行有返回值任务,
         * public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier){..}
         * public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier,Executor executor){..}
         * 同时支持传入自定义的线程池，如果不传入线程池的话默认是使用 ForkJoinPool.commonPool()作为它的线程池执行异步代码
         */
        //使用自定义的线程池
        Executor executor = Executors.newFixedThreadPool(10);
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() ->{
            log.info("---开始异步线程执行任务----");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("---异步线程执行任务结束----");
            return "CompletableFuture returned result";
        }, executor);

        // 设置回调（监听）如果执行成功:
        cf.thenAccept( result -> {
            log.info("异步通知的结果是:{}",result);
        }).exceptionally((e)->{
            log.info("异步执行产生了异常,异常信息是:{}",e.getMessage());
            return null;
        });
        log.info("----主线程无需阻塞等待,继续执行其他业务");
        TimeUnit.SECONDS.sleep(10);
    }

    /**
     * CompletableFuture的优点是：
     *
     * 1、异步任务结束时，会自动回调某个对象的方法；
     * 2、异步任务出错时，会自动回调某个对象的方法；
     * 3、主线程设置好回调后，不再关心异步任务的执行。
     * 另外注意CompletableFuture 某些系列方法的命名规则：
     *  xxx()：表示该方法将继续在已有的线程中执行；
     *  xxxAsync()：表示该方法在另外的线程池中执行(特别针对的是指定了自定义线程池后,xxxAsyn()方法会在CompletableFuture内部默认的ForkJoinPool中执行)
     *
     *  如果只是实现了异步回调机制，我们还看不出CompletableFuture相比Future的优势。
     *  CompletableFuture更强大的功能是，多个CompletableFuture可以串行执行，可以并行执行，合并两个异步任务，下一个依赖上一个的结果等等，
     *
     */

    @Test
    public void testSerialize() throws InterruptedException {
        //测试多个CompletableFuture串行执行
        //第一个任务
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            log.info("---第一个异步线程执行任务----,time={}", LocalDateTime.now().toString());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("---第一个异步线程执行任务结束----,time={}", LocalDateTime.now().toString());
            return "hello";
        });
        //第一个任务成功后继续执行下一个任务
        CompletableFuture<String> cf2 = cf.thenApply((pre) -> {
            log.info("---第二个异步线程执行任务,接收到的第一个异步线程的执行结果是:{},time={}", pre, LocalDateTime.now().toString());
            return pre + " word";
        });

        //设置回调
        cf2.thenAccept((result)->{
            log.info("两个异步任务总的结果是:{}",result);
        }).exceptionally((e)->{
            log.info("异步执行产生了异常,异常信息是:{}",e.getMessage());
            return null;
        });

        log.info("----主线程无需阻塞等待,继续执行其他业务");
        TimeUnit.SECONDS.sleep(10);
    }


    @Test
    public void testAnyOf() throws InterruptedException {
        //测试多个CompletableFuture 并行执行; 只要其中任意一个返回就可以继续往下执行
        //第一个任务
        CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
            log.info("---第一个异步线程执行任务开始----,time={}", LocalDateTime.now().toString());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("---第一个异步线程执行任务结束----,time={}", LocalDateTime.now().toString());
            return "hello";
        });
        //第二个任务
        CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(() -> {
            log.info("---第二个异步线程执行任务开始----,time={}", LocalDateTime.now().toString());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("---第二个异步线程执行任务结束----,time={}", LocalDateTime.now().toString());
            return " word ";
        });

        /**
         * 将两个CompletableFuture合并为一个新的CompletableFuture
         * anyOf()和allOf()用于并行化多个CompletableFuture。
         * anyof:两个任务任意一个返回即产生回调
          */
        CompletableFuture<Object> cf3 = CompletableFuture.anyOf(cf1, cf2);
        //设置回调
        cf3.thenAccept((result)->{
            log.info("两个异步任务的结果是:{}",result);
        }).exceptionally((e)->{
            log.info("异步执行产生了异常,异常信息是:{}",e.getMessage());
            return null;
        });


        log.info("----主线程无需阻塞等待,继续执行其他业务");
        TimeUnit.SECONDS.sleep(10);
    }


    @Test
    public void testCombine() throws InterruptedException {
        /**
         * 如果有两个任务需要异步执行，且后面需要对这两个任务的结果进行合并处理，CompletableFuture 也支持这种处理：
         */
        Executor executor = Executors.newFixedThreadPool(10);
        //第一个任务
        CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
            log.info("---第一个异步线程执行任务开始----,time={}", LocalDateTime.now().toString());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("---第一个异步线程执行任务结束----,time={}", LocalDateTime.now().toString());
            return "hello";
        },executor);
        //第二个任务
        CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(() -> {
            log.info("---第二个异步线程执行任务开始----,time={}", LocalDateTime.now().toString());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("---第二个异步线程执行任务结束----,time={}", LocalDateTime.now().toString());
            return " word ";
        },executor);

        //对两个任务的结果进行合并
        CompletableFuture<String> cf3 = cf1.thenCombineAsync(cf2, (task1, task2) -> {
            log.info("两个异步任务的结果分别是{}:{}", task1, task2);
            return task1 + "******" + task2;
        });
        //设置回调
        cf3.thenAcceptAsync((totalResult)->{
            log.info("两个异步任务合并后的结果是:{}",totalResult);
        }).exceptionally((e)->{
            log.info("异步执行产生了异常,异常信息是:{}",e.getMessage());
            return null;
        });


        log.info("----主线程无需阻塞等待,继续执行其他业务");
        TimeUnit.SECONDS.sleep(10);
    }

    /**
     * 常用 API 介绍
     * 1、拿到上一个任务的结果做后续操作，上一个任务完成后的动作
     * public CompletableFuture<T>     whenComplete(BiConsumer<? super T,? super Throwable> action)
     * public CompletableFuture<T>     whenCompleteAsync(BiConsumer<? super T,? super Throwable> action)
     * public CompletableFuture<T>     whenCompleteAsync(BiConsumer<? super T,? super Throwable> action, Executor executor)
     * public CompletableFuture<T>     exceptionally(Function<Throwable,? extends T> fn)
     * 上面四个方法表示在当前阶段任务完成之后下一步要做什么。
     *
     * 2、拿到上一个任务的结果做后续操作，使用 handler 来处理逻辑，可以返回与第一阶段处理的返回类型不一样的返回类型。
     * public <U> CompletableFuture<U>  handle(BiFunction<? super T,Throwable,? extends U> fn)
     * public <U> CompletableFuture<U>  handleAsync(BiFunction<? super T,Throwable,? extends U> fn)
     * public <U> CompletableFuture<U>  handleAsync(BiFunction<? super T,Throwable,? extends U> fn, Executor executor)
     * Handler 与 whenComplete 的区别是 handler 是可以返回一个新的 CompletableFuture 类型的。
     *
     * CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> {
     *     return "hahaha";
     * }).handle((r, e) -> {
     *     return 1;
     * });
     *
     *
     * 3、拿到上一个任务的结果做后续操作， thenApply方法
     * public <U> CompletableFuture<U>  thenApply(Function<? super T,? extends U> fn)
     * public <U> CompletableFuture<U>  thenApplyAsync(Function<? super T,? extends U> fn)
     * public <U> CompletableFuture<U>  thenApplyAsync(Function<? super T,? extends U> fn, Executor executor)
     * 注意到 thenApply 方法的参数中是没有 Throwable，这就意味着如有有异常就会立即失败，不能在处理逻辑内处理。且 thenApply 返回的也是新的 CompletableFuture。 这就是它与前面两个的区别。
     *
     * 4、拿到上一个任务的结果做后续操作，可以不返回任何值，thenAccept方法
     * public CompletableFuture<Void>  thenAccept(Consumer<? super T> action)
     * public CompletableFuture<Void>  thenAcceptAsync(Consumer<? super T> action)
     * public CompletableFuture<Void>  thenAcceptAsync(Consumer<? super T> action, Executor executor)
     * 看这里的示例：
     *
     * CompletableFuture.supplyAsync(() -> {
     *   return "result";
     * }).thenAccept(r -> {
     *   System.out.println(r);
     * }).thenAccept(r -> {
     *   System.out.println(r);
     * });
     * 执行完毕是不会返回任何值的。
     *
     * CompletableFuture 的特性提现在执行完 runAsync 或者 supplyAsync 之后的操作上。
     * CompletableFuture 能够将回调放到与任务不同的线程中执行，也能将回调作为继续执行的同步函数，在与任务相同的线程中执行。它避免了传统回调最大的问题，那就是能够将控制流分离到不同的事件处理器中。
     *
     * 另外当你依赖 CompletableFuture 的计算结果才能进行下一步的时候，无需手动判断当前计算是否完成，可以通过 CompletableFuture 的事件监听自动去完成。
     *
     */
}
