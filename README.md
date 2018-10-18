# Guice-Util
Additional Guice scoping machinery. [Javadoc here.](https://tembrel.github.io/Guice-Util/javadoc/)

[My original blog post](https://tembrel.blogspot.com/2009/11/concurrently-initialized-singletons-in.html) explains the motivation. I've reproduced it here, as well.

## Original post

William Pietri [asked](
  http://groups.google.com/group/google-guice/browse_thread/thread/d56f336baae2113e?tvc=2"
) whether it would be possible to take advantage of concurrency and Guice's knowledge of dependencies to provide interdependent (and expensive to create) singleton services in parallel.

For example, say that service A depends on services B and C and that B and C don't know about each other (and so could be constructed concurrently). Obviously, you could explicitly construct B and C in separate tasks running in a thread pool, then construct A once B and C exist.

```java
class A {
    A(B b, C c) { /* ... use b and c ... */ }
}

class B {
    B() { /* ... takes a long time ... */ }
}

class C {
    C() { /* ... takes a long time ... */ }
}

class Services {
    A a; B b; C c;
    void init() throws InterruptedException {
        final CountDownLatch ready = new CountDownLatch(2);
        Executor pool = Executors.newCachedThreadPool();
        pool.execute(new Runnable() {
            public void run() { b = new B(); ready.countDown(); }
        })
        pool.execute(new Runnable() {
            public void run() { c = new C(); ready.countDown(); }
        })
        ready.await();
        a = new A(b, c);
        // Now publish a, b, and c safely -- not shown.
    }
}
```
But it's a pain to have to orchestrate this yourself. You have to know the dependencies intimately and write a lot of tricky code. If the dependencies change, the code has to be completely rethought. Wouldn't it be nice if Guice could do this for you automatically without giving up on the opportunities for concurrency? Well, it turns out that it can.

The idea is to use a variant of singleton scope that runs the providers of all bindings for that scope in separate threads. Naturally some of these providers block in their own thread while their dependencies finish in other threads, but those services that __can__ be constructed in parallel __will__ be.

Here's what the Guice version looks like:
```java
@ConcurrentSingleton
class A {
    @Inject A(B b, C c) { /* ... use b and c ... */ }
}

@ConcurrentSingleton
class B {
    @Inject B() { /* ... takes a long time ... */ }
}

@ConcurrentSingleton
class C {
    @Inject C() { /* ... takes a long time ... */ }
}
```
No special initialization code needed, just inject A, B, and C wherever they are needed. (Note that if you have circular dependencies, this probably won't work. So don't have circular dependencies.)

[ConcurrentSingleton](
  https://github.com/Tembrel/Guice-Util/blob/master/src/main/java/net/peierls/guiceutil/ConcurrentSingleton.java
) is just a scope annotation. The actual scope implementation is [ConcurrentSingletonScope](https://github.com/Tembrel/Guice-Util/blob/master/src/main/java/net/peierls/guiceutil/ConcurrentSingletonScope.java
). I have a fleshed-out version of the A, B, C example, [ConcurrentSingletonExample](https://github.com/Tembrel/Guice-Util/blob/master/src/test/java/net/peierls/guiceutil/ConcurrentSingletonExample.java
), that is slightly different from the code above; it uses Providers for B and C so that A can do some work before getting B and C in the constructor.

You aren't forced to do all the expensive work in the constructor. You could also inject a start method with injected parameters to provide needed dependencies.

In the same thread that provoked the ConcurrentSingleton response, Jesse Wilson suggested the use of the experimental lifecycle facility in Guava. This could prove useful in the current context if, for example, you have singleton services that take a while to stop and could benefit from being stopped in parallel.

### Update 2009-11-30 ###

I added a call to <code>pool.shutdown()</code> in the scope implementation to make sure the pool threads don't prevent the JVM from exiting. It might not be necessary, but I don't think it hurts.

### Update 2010-7-3 ###

Added explicit notice that the sources are in the public domain.

### Update 2018-10-16 ###

Added a [GitHub repository](
  https://github.com/Tembrel/Guice-Util
) with the concurrent singleton code and other utilities, as the old links to code weren't working any more. Now using MIT open source license.

