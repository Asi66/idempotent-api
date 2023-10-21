# 一、背景
最近在接口测试过程中，发现管理端接口多次提交，进而导致出现重复数据的情况。
# 二、目标
确保接口幂等性。
接口幂等性就是用户对于同一操作发起的一次请求或者多次请求的结果是一致的。
# 三、主要思路
一般要确保幂等性的接口都是涉及修改或新增的操作。
利用Spring的AOP拦截指定接口，指定接口方法加上注解标识，然后将接口token+请求方式+请求路径作为分布式锁的key。用户请求一进来就设置redis锁，后续在锁时间内重复请求提交即抛异常处理。
# 三、代码参考

 - JDK21
 - springboot 3.1.5
## 主要代码
接口层
```java
package com.asi.idempotent.api;

import com.asi.idempotent.application.UpdateTestCommand;
import com.asi.idempotent.infra.resubmit.Resubmit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author asi
 * @date 2023/10/7 15:50
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    @PostMapping("/update")
    @Resubmit
    public String update(@RequestBody UpdateTestCommand command){
        log.info("修改参数：{}",command.toString());
        return "SUCCESS";
    }
}

```
防重提交注解
```java
package com.asi.idempotent.infra.resubmit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author asi
 * @date 2023/10/7 16:03
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Resubmit {
    /**
     * 防重提交锁过期时间(秒)
     * 默认5秒内不允许重复提交
     */
    int expire() default 5;
}
```
防重提交注解拦截器
```java
package com.asi.idempotent.infra.resubmit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author asi
 * @date 2023/10/7 16:06
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ResubmitAspect {

    private final RedissonClient redissonClient;

    private static final String RESUBMIT_LOCK_PREFIX = "LOCK:RESUBMIT:";

    /**
     * 请求头Token的Key
     */
    private static final String TOKEN_KEY = "Authorization";

    /**
     * 防重复提交切点
     */
    @Pointcut("@annotation(resubmit)")
    public void preventDuplicateSubmitPointCut(Resubmit resubmit) {
    }

    @Around(value = "preventDuplicateSubmitPointCut(resubmit)", argNames = "pjp,resubmit")
    public Object doAround(ProceedingJoinPoint pjp, Resubmit resubmit) throws Throwable {
        String resubmitLockKey = generateResubmitLockKey();
        if (resubmitLockKey != null) {
            // 防重提交锁过期时间
            int expire = resubmit.expire();
            RLock lock = redissonClient.getLock(resubmitLockKey);
            // 获取锁失败，直接返回 false
            boolean lockResult = lock.tryLock(0, expire, TimeUnit.SECONDS);
            if (!lockResult) {
                // 抛出重复提交提示信息
                throw new RuntimeException("您的请求已提交，请不要重复提交或等待片刻再尝试。");
            }
        }
        return pjp.proceed();
    }


    /**
     * 获取防重提交锁的 key
     */
    private String generateResubmitLockKey() {
        String resubmitLockKey = null;
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String token = request.getHeader(TOKEN_KEY);
        if (StringUtils.hasText(token)) {
            //防重前缀 + token + 请求方式 + 请求路径
            resubmitLockKey = RESUBMIT_LOCK_PREFIX + token + ":" + request.getMethod() + "-" + request.getRequestURI() + "-";
        }
        return resubmitLockKey;
    }
}
```
...
详细代码请看：https://github.com/Asi66/Idempotent
