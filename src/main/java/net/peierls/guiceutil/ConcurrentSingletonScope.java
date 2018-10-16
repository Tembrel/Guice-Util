package net.peierls.guiceutil;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Scope;
import com.google.inject.spi.DefaultBindingScopingVisitor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.inject.Inject;


/**
 * A special form of singleton scope that eagerly starts building its
 * singletons concurrently in background using a thread pool at injector
 * creation.
 */
public final class ConcurrentSingletonScope implements Scope {

    // Scope method

    @Override public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {

        // Memoize call to unscoped.get() under key.

        return new Provider<T>() {
            @Override public T get() {
                ConcurrentMap<Key<T>, Future<T>> futures = futures();
                Future<T> f = futures.get(key);
                if (f == null) {
                    FutureTask<T> creator = new FutureTask<T>(() -> unscoped.get());
                    f = futures.putIfAbsent(key, creator);
                    if (f == null) {
                        f = creator;
                        creator.run();
                    }
                }
                try {
                    return f.get();
                } catch (ExecutionException e) {
                    Throwable t = e.getCause();
                    if (t instanceof Error) {
                        throw (Error) t;
                    }
                    // Can't be a checked exception, so we cast.
                    throw (RuntimeException) e.getCause();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ProvisionException("interrupted during provision", e);
                }
            }
        };
    }

    //
    // Implementation
    //

    ConcurrentSingletonScope() {
        // Prevent instantiation outside of package.
    }

    @Inject private void initializeBindingsInThisScope(Injector injector) { // NOPMD
        final Scope thisScope = this;
        final ExecutorService pool = Executors.newCachedThreadPool();
        for (final Binding<?> binding : injector.getBindings().values()) {
            binding.acceptScopingVisitor(new DefaultBindingScopingVisitor<Void>() {
                @Override public Void visitScope(Scope scope) {
                    // OK to compare by identity.
                    if (scope == thisScope) { // NOPMD
                        pool.execute(new Runnable() {
                            @Override public void run() {
                                binding.getProvider().get();
                            }
                        });
                    }
                    return null;
                }
            });
        }
        pool.shutdown();
    }

    @SuppressWarnings("unchecked")
    <T> ConcurrentMap<Key<T>, Future<T>> futures() {
        return (ConcurrentMap<Key<T>, Future<T>>) futures;
    }

    private final ConcurrentMap futures = new ConcurrentHashMap();
}
