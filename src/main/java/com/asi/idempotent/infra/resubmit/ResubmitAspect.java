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
