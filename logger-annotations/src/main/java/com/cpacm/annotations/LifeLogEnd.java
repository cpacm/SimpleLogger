package com.cpacm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *
 * @author cpacm 2019-10-25
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface LifeLogEnd {
}
