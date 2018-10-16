package net.peierls.guiceutil;

import com.google.inject.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.junit.*;
import static org.junit.Assert.*;


public final class SingletonScopesTest {

    @Test public void testLazySingletonProduction() {
        testLazySingleton(Stage.PRODUCTION);
    }

    @Test public void testLazySingletonDevelopment() {
        testLazySingleton(Stage.DEVELOPMENT);
    }

    @Test public void testSingletonProduction() {
        testSingleton(Stage.PRODUCTION);
    }

    @Test public void testSingletonDevelopment() {
        testSingleton(Stage.DEVELOPMENT);
    }

    @Test public void testEagerSingletonProduction() {
        testEagerSingleton(Stage.PRODUCTION);
    }

    @Test public void testEagerSingletonDevelopment() {
        testEagerSingleton(Stage.DEVELOPMENT);
    }

    void testLazySingleton(Stage stage) {
        final AtomicInteger injectionCount = new AtomicInteger();

        assertEquals(0, injectionCount.get());

        Injector inj = Guice.createInjector(
            stage,
            new AbstractModule() {
                @Override protected void configure() {
                    bind(LazyObject.class);
                    bind(AtomicInteger.class).toInstance(injectionCount);
                }
            },
            new SingletonScopesModule()
        );

        assertEquals(0, injectionCount.get());

        Provider<LazyObject> provider = inj.getProvider(LazyObject.class);

        assertEquals(0, injectionCount.get());

        LazyObject obj = provider.get();

        assertEquals(1, injectionCount.get());

        LazyObject obj2 = provider.get();

        assertEquals(1, injectionCount.get());

        assertSame(obj, obj2);
        assertEquals(1, obj.injectionCount);
    }

    void testSingleton(Stage stage) {
        final AtomicInteger injectionCount = new AtomicInteger();

        assertEquals(0, injectionCount.get());

        Injector inj = Guice.createInjector(
            stage,
            new AbstractModule() {
                @Override protected void configure() {
                    bind(NormalObject.class);
                    bind(AtomicInteger.class).toInstance(injectionCount);
                }
            },
            new SingletonScopesModule()
        );

        // initial expectation:
        int countAfterInjection = stage == Stage.PRODUCTION ? 1 : 0;

        assertEquals(countAfterInjection, injectionCount.get());

        Provider<NormalObject> provider = inj.getProvider(NormalObject.class);

        assertEquals(countAfterInjection, injectionCount.get());

        NormalObject obj = provider.get();

        assertEquals(1, injectionCount.get());

        NormalObject obj2 = provider.get();

        assertEquals(1, injectionCount.get());

        assertSame(obj, obj2);
        assertEquals(1, obj.injectionCount);
    }

    void testEagerSingleton(Stage stage) {
        final AtomicInteger injectionCount = new AtomicInteger();

        assertEquals(0, injectionCount.get());

        Injector inj = Guice.createInjector(
            stage,
            new AbstractModule() {
                @Override protected void configure() {
                    bind(EagerObject.class).asEagerSingleton();
                    bind(AtomicInteger.class).toInstance(injectionCount);
                }
            },
            new SingletonScopesModule()
        );

        assertEquals(1, injectionCount.get());

        Provider<EagerObject> provider = inj.getProvider(EagerObject.class);

        assertEquals(1, injectionCount.get());

        EagerObject obj = provider.get();

        assertEquals(1, injectionCount.get());

        EagerObject obj2 = provider.get();

        assertEquals(1, injectionCount.get());

        assertSame(obj, obj2);
        assertEquals(1, obj.injectionCount);
    }

    @LazySingleton
    static class LazyObject {
        final int injectionCount;
        @Inject LazyObject(AtomicInteger injectionCount) {
            this.injectionCount = injectionCount.incrementAndGet();
        }
    }

    @Singleton
    static class NormalObject {
        final int injectionCount;
        @Inject NormalObject(AtomicInteger injectionCount) {
            this.injectionCount = injectionCount.incrementAndGet();
        }
    }

    static class EagerObject {
        final int injectionCount;
        @Inject EagerObject(AtomicInteger injectionCount) {
            this.injectionCount = injectionCount.incrementAndGet();
        }
    }
}
