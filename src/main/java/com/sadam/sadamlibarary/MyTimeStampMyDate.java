package com.sadam.sadamlibarary;

import java.sql.Timestamp;
import java.util.Date;

/**是用来替代java.util.Date类。继承Date类的所有功能。用它不用出现手动换算，比如优化了getYear()方法的加1900才能求出正确时间的短板。
 *
 *
 */
public class MyTimeStampMyDate extends Timestamp {

    public MyTimeStampMyDate() {
        super(new Date().getTime());
    }

    public MyTimeStampMyDate(int year, int month, int date) {
        this(year, month, date,0,0);
    }

    public MyTimeStampMyDate(int year, int month, int date, int hrs, int min) {
        this(year, month, date, hrs, min,0);
    }

    public MyTimeStampMyDate(int year, int month, int date, int hrs, int min, int sec) {
        super(new Date(year-1900, month-1, date, hrs, min, sec).getTime());
    }

    public MyTimeStampMyDate(Date date){
        super(date.getTime());
    }

    public MyTimeStampMyDate(long deadline) {
        super(deadline);
    }


    @Override
    public int getYear() {
        return super.getYear()+1900;
    }

    @Override
    public int getMonth() {
        return super.getMonth()+1;
    }
}
