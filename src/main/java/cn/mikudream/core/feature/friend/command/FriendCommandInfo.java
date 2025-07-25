package cn.mikudream.core.feature.friend.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface FriendCommandInfo {
    String name();

    String purpose();

    String syntax() default "";
}
