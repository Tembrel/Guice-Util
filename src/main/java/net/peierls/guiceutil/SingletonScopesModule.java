package net.peierls.guiceutil;

import com.google.inject.AbstractModule;
import com.google.inject.Scope;


/**
 * Install LazySingletonScope and ConcurrentSingletonScope.
 */
public class SingletonScopesModule extends AbstractModule {

    @Override protected void configure() {

        Scope lscope = new LazySingletonScope();
        binder().bindScope(LazySingleton.class, lscope);

        Scope cscope = new ConcurrentSingletonScope();
        binder().bindScope(ConcurrentSingleton.class, cscope);
        binder().requestInjection(cscope); // scans bindings for eager inits
    }
}
