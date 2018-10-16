package net.peierls.guiceutil;

import java.util.concurrent.TimeUnit;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;


/**
 * An artificial example of using ConcurrentSingletonScope.
 */
public final class ConcurrentSingletonExample {

    public static void main(String... args) throws InterruptedException {
        long start = System.nanoTime();

        Injector injector = Guice.createInjector(
            new SingletonScopesModule(),
            new AbstractModule() {
                @Override public void configure() {
                    bind(A.class);
                    bind(B.class);
                    bind(C.class);
                }
            }
        );

        A a = injector.getInstance(A.class); // NOPMD

        long elapsed = System.nanoTime() - start;

        System.out.printf("Completed in %d seconds%n", TimeUnit.NANOSECONDS.toSeconds(elapsed));
    }

    // Concurrently provided "services" A, B, and C. A depends on B and C.

    @ConcurrentSingleton
    static class A {
        @Inject public A() {
            try {
                System.out.printf("Starting A on thread %s%n", Thread.currentThread().getName());
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                System.out.println("Started A");
            }
        }

        @Inject void start(B b, C c) {
            System.out.printf("A getting B and C instances on thread %s%n", Thread.currentThread().getName());
            //b.get();
            //c.get();
        }
    }

    @ConcurrentSingleton
    static class B {
        @Inject public B() {
            try {
                System.out.printf("Starting B on thread %s%n", Thread.currentThread().getName());
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                System.out.println("Started B");
            }
        }
    }

    @ConcurrentSingleton
    static class C {
        @Inject public C() {
            try {
                System.out.printf("Starting C on thread %s%n", Thread.currentThread().getName());
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                System.out.println("Started C");
            }
        }
    }


    private ConcurrentSingletonExample() { /* uninstantiable */ }
}