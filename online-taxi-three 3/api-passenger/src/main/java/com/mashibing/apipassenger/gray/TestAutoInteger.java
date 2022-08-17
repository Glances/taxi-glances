package com.mashibing.apipassenger.gray;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询 的rule
 */
public class TestAutoInteger {
    static AtomicInteger nextServerCyclicCounter = new AtomicInteger(0);;
    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            ext();
        }


    }

    static void ext(){
        int current = nextServerCyclicCounter.get();
        System.out.println("current："+current);
        int next = (current + 1) % 10;
        System.out.println("next"+next);
        if (nextServerCyclicCounter.compareAndSet(current, next)){
            System.out.println("set 后："+nextServerCyclicCounter.get());
            System.out.println("-"+next);
        }
    }
}
