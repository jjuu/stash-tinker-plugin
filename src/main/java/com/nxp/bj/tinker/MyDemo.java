package com.nxp.bj.tinker;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyDemo {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        
        Date now = new Date();
        System.out.println(ft.format(now));
    }
}
