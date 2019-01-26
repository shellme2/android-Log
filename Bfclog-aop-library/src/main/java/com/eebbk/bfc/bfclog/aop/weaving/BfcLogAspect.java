package com.eebbk.bfc.bfclog.aop.weaving;


import com.eebbk.bfc.bfclog.BfcLogger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.concurrent.TimeUnit;

/**
 *
 * 具体的api参考 http://www.eclipse.org/aspectj/doc/released/runtime-api/index.html
 *
 * Created by Simon on 2016/9/6.
 */
@Aspect
public class BfcLogAspect {
//    private static final String TAG = BfcLogAspect.class.getSimpleName();
    private static final String TAG = "Time";
    private static volatile boolean enable = true;

	@Pointcut("within(@com.eebbk.bfc.bfclog.annotation.BfcLogTime *)")
	public void withinAnnotatedClass(){}

	@Pointcut("execution(!synthetic * *(..)) && withinAnnotatedClass()")
	public void methodInsideAnnotatedType() {}

	@Pointcut("execution(!synthetic *.new(..)) && withinAnnotatedClass()")
	public void constructorInsideAnnotatedType() {}

	@Pointcut("execution(@com.eebbk.bfc.bfclog.annotation.BfcLogTime * *(..)) || methodInsideAnnotatedType()")
	public void method() {}

	@Pointcut("execution(@com.eebbk.bfc.bfclog.annotation.BfcLogTime *.new(..)) || constructorInsideAnnotatedType()")
	public void constructor() {}


	public static void setEnable(boolean enable) {
		BfcLogAspect.enable = enable;
	}

	@Around("method() || constructor()")
	public Object logTimeExecute(ProceedingJoinPoint joinPoint) throws Throwable{
         CodeSignature signature = (CodeSignature) joinPoint.getSignature();

        String[] paramsNames = signature.getParameterNames();
        Object[] paramsValues = joinPoint.getArgs();

        StringBuilder sb = new StringBuilder();
        int N = paramsNames.length;
        for (int i =0; i < N; i ++){
            if (i == 0){
                sb.append("⤵").append(" ");
            }else{
                sb.append(", ");
            }
            sb.append( paramsNames[i] ).append("=").append( paramsValues[i] );
            if (i == N-1){
                sb.append( "\n" );
            }
        }

		long startNanos = System.nanoTime();
		Object result = joinPoint.proceed();
		long stopNanos = System.nanoTime();
        long lenghtMilis = TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos);

        boolean hasReturnType = signature instanceof MethodSignature
                && ((MethodSignature) signature).getReturnType() != void.class;

        if (hasReturnType){
            sb.append("⤶");
            sb.append(" ").append(result);
            sb.append("\n");
        }

        sb.append("⥂").append(" ")
                .append( lenghtMilis ).append("ms");

        BfcLogger.d(TAG, sb.toString());
//        BfcInnerLog.d(  sb.toString() );

		return result;
	}

/*    @Pointcut("execution(@com.eebbk.bfc.bfclog.annotation.BfcLogAround * *(..)) && @annotation(ann)")
    public void bfcLogAround(BfcLogAround ann){}

    @Around("bfcLogAround(anno)")
    public Object bfcLogAroundExecute(ProceedingJoinPoint joinPoint, BfcLogAround anno) throws Throwable {
        Object result = joinPoint.proceed();

        BfcLogger.e("zy", "---------------" + anno.uniqueId());
        return result;
    }*/

}
