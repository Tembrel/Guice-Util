package net.peierls.guiceutil;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;


/**
 * A special form of singleton scope that won't eagerly
 * produce an instance even when the Stage is PRODUCTION.
 */
public final class LazySingletonScope implements Scope {

    // Scope method

    @Override public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {

        return new Provider<T>() {

            volatile T instance = null;

            @Override public T get() {
                T tmp = instance;
                if (tmp == null) {
                    synchronized (this) {
                        tmp = instance;
                        if (tmp == null) {
                            instance = tmp = unscoped.get();
                        }
                    }
                }
                return tmp;
            }
        };
    }

    //
    // Implementation
    //

    LazySingletonScope() {
        // Prevent instantiation outside of package.
    }
}
