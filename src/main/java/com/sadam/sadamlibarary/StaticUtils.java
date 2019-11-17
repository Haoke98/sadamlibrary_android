package com.sadam.sadamlibarary;

public class StaticUtils {
    private static final byte MAXFLOOR=3;
    public static String getCodeInfo(Throwable throwable){
        String s = "";
        int i=0;
        for (StackTraceElement stackTraceElement:throwable.getStackTrace()){
            if(i==0){
                i++;
                continue;
            }
            s+=stackTraceElement.toString()+""+"\n";
            if(i++==MAXFLOOR){
                break;
            }
        }
        return s+":";
    }
}
