package com.example.demo.ketang;

import java.util.Optional;

public class OptionTest {
    public static void main(String[] args) {
        Integer meters = null;
        Integer a = Optional.ofNullable(meters).orElse(1);
        System.out.println(a);
    }
}
