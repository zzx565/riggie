package cn.zzx.reggie.common;



// 基于ThreadLocal封装工具类，获取用户登录的id
/*
ThreadLocal并不是一个Thread，而是Thread的局部变量。当使用ThreadLocal维护变量时，ThreadLocal为每个使用该变量的线程提供独立的变量
副本，所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本ThreadLocal为每个线程提供单独一份存储空间，具有线程隔
离的效果，只有在线程内才能获取到对应的值，线程外则不能访问。
 */

public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }


}
