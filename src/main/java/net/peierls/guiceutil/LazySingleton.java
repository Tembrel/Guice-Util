package net.peierls.guiceutil;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

import com.google.inject.Injector;
import javax.inject.Scope;

/**
 * Apply this to implementation classes when you want only one instance
 * (per {@link Injector}) to be reused for all injections for that binding,
 * and you want to create the instance lazily.
 */
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RUNTIME)
@Scope
public @interface LazySingleton {}
