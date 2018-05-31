package de405;

public class JuliusDay
{
 public static void main(String args[])
  {
   double year = 1660;
   double month = 10;
   double day = 1;
   double hour = 0;
   double minute = 0;
   double second = 0;
   double start_epoch = 2305424.50;
   double step = 32;

   if(month < 3.0)
    {
     month += 12;
     year -= 1;
    }
   double a = Math.floor(year / 100);
   double b = 2 - a +  Math.floor(a / 4);

   double JD = Math.floor(365.25 * year) + Math.floor(30.6001 *(month + 1))+ day + b + 1720994.5 + hour /24.0 + minute / 1440 + second / 86400;
   if(2299160.5 > JD) JD = JD - b;

   System.out.println("julius Day = " + JD);

   double record_index = Math.floor((JD - start_epoch) / step);

   System.out.println("record_index = " + record_index);
  }
}
