package de405;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class DE405Reader
 {
  private static final int step = 32;
  private static final int INDEX_SATURN = 5;
  private static final int INDEX_SUN = 10;
  private static final int INDEX_SHODO = 11;
  private static final int POSITION = 3;
  private static final int SHODO = 2;
  private static final int NCOEFF =  1018;

  private static final int OFFSET = 0;
  private static final int NUM_OF_COEFF = 1;
  private static final int DIVISION = 2;

  private static final int START_JD = 1;
  private static final int FINAL_JD = 2;
  private static final String AU= "0.149597870691000015E+09";

  private static final double startepoch = 2305424.5; //1599 DEC 09 00:00:00
  private static final double finalepoch = 2525008.5;//FEB 20 00:00:00

  private static final String de405directory="de405files";

  private static final String[][] de405files = new String[][]{
   { "ascp2000.405", "0.245153650000000000E+07", "0.245886450000000000E+07" },
   { "ascp2020.405", "0.245883250000000000E+07", "0.246616050000000000E+07" }
   };
/*
  private static final String[] filenames= new String[]{
   "ascp1600.405", "ascp1620.405" ,"ascp1640.405", "ascp1660.405",
   "ascp1680.405", "ascp1700.405", "ascp1720.405", "ascp1740.405",
   "ascp1760.405", "ascp1780.405", "ascp1800.405", "ascp1820.405",
   "ascp1840.405", "ascp1860.405", "ascp1880.405", "ascp1900.405",
   "ascp1920.405", "ascp1940.405", "ascp1960.405", "ascp1980.405",
   "ascp2000.405", "ascp2020.405", "ascp2040.405" };

  private static int[] records = new int[] {
   229, 229, 229, 229,
   230, 229, 229, 229,
   230, 229, 229, 230,
   229, 229, 229, 230,
   229, 229, 229, 230,
   229, 229, 230 };
*/
  private static int[][] format = new int[][]{
   {   3, 14, 4 }, // Mercury
   { 171, 10, 2 }, //Venus
   { 231, 13, 2 },//Earth-Moon barycenter
   { 309, 11, 1 },//Mars
   { 342,  8, 1 },//Jupiter
   { 366,  7, 1 },//Saturn
   { 387,  6, 1 },//Uranus
   { 405,  6, 1 },//Neptune
   { 423,  6, 1 },//Pluto
   { 441, 13, 8 },//Moon (geocentric)
   { 753, 11, 2 },//Sun
   { 819, 10, 4 },//
   { 899, 10, 4 },//
  };

 class Coefficients
  {
   public double[][] data;

   public Coefficients()
    {
     data = new double[format.length][];

     for (int i = 0;i < format.length; i++)
      {
       int numofcoeff= format[i][1];
       int division = format[i][2];
       if (i != INDEX_SHODO) data[i] = new double[numofcoeff * POSITION * division];
       else data[i] = new double[numofcoeff * SHODO * division];
      }
    }
  }


/*
GROUP   1050

     3   171   231   309   342   366   387   405   423   441   753   819   899
    14    10    13    11     8     7     6     6     6    13    11    10    10
     4     2     2     1     1     1     1     1     1     8     2     4     4
*/

  public static void main(String[] args)
   {
    //データのインデックスを計算する
    double year = 2010;
    double month = 9;
    double day = 1;
    double hour = 0;
    double minute = 0;
    double second = 0;
    double JD =  getJuliusDay(year, month, day, hour, minute, second);

    System.out.println("julius Day = " + JD);
    String filename = "";
    double startJD = 0;

    for (int i = 0; i < de405files.length;i++)
     {
      double start = Double.parseDouble(de405files[i][START_JD]);
      double end   = Double.parseDouble(de405files[i][FINAL_JD]);

      if (start <= JD & JD <= end)
       {
        startJD = start;
        filename = de405files[i][0];
        break;
       }
     }

    double record_index = Math.floor((JD - startJD) / step); //0からの数字
    try
     {
      double[] values = readRecord( filename, (int)record_index);
      double sunz = getSunPosition(values, JD, INDEX_SUN);
      double satz = getSunPosition(values, JD, INDEX_SATURN);

      double result = (sunz - satz) / Double.parseDouble(AU);
      System.out.println("result = " + result);
     }
    catch(Exception e){ System.out.println(e.toString());}

    System.out.println("difference in day = " + (JD - startJD));
    System.out.println("record_index = " + record_index + " days = " + ((record_index + 1) * step));
    System.out.println("filename = " + filename );
   }


  //レコードを読む
  private static double[] readRecord(String name, int recordnumber)throws IOException
   {
    String path = new File(".").getAbsoluteFile().getParent()+ File.separator + de405directory + File.separator;

    BufferedReader reader = new BufferedReader(new FileReader(new File(path + name)));

    int lines = NCOEFF / 3;
    lines +=  (NCOEFF  % 3) > 0 ? 1 : 0;
    System.out.println("lines = " + lines);

    //所定の位置までスキップする
    if (recordnumber > 0)
     {
      for (int i = 0; i < (recordnumber * (lines + 1)); i++ ) reader.readLine();
     }

    //読み込みを開始
    reader.readLine(); //インデックスの行をスキップ
    double[] values = new double[NCOEFF];

    for ( int i = 0, count = 0;i < lines; i++)
     {
      String str = reader.readLine().trim();
      int blank = str.indexOf(" ");

      String strval = str.substring(0, blank).replace("D", "E");
      values[count++] = Double.parseDouble(strval);
      str = str.substring(blank, str.length()).trim();
      if (count >= NCOEFF) break;

      blank = str.indexOf(" ");
      strval = str.substring(0, blank).replace("D", "E");
      values[count++] = Double.parseDouble(strval);
      str = str.substring(blank, str.length()).trim();
      if (count >= NCOEFF) break;

      values[count++] = Double.parseDouble(str.replace("D", "E"));
      if (count >= NCOEFF) break;
     }

    return  values;
   }


  //係数から位置を計算する
  private static double getSunPosition(double[] values, double jd, int index)
   {
    double start = values[0];
    double finish = values[1];

    if (start<= jd & jd <= finish)
     {
      int offset = format[index][OFFSET];
      int numofcoeff = format[index][NUM_OF_COEFF];
      int division = format[index][DIVISION];

      double subdivision = (finish - start) / division;

      double quotient = ((jd - start) / subdivision);
      int pos = (int)Math.floor(quotient);
      double t = quotient - pos;
      double[][] coefficient = new double[3][numofcoeff];
      offset = offset + (pos * numofcoeff * 3) -1;

      t = 2.0 * t - 1.0;//正規化

      for (int i = 0; i < 3; i++)
       {
        for (int j = 0; j < numofcoeff; j++)
         {
          coefficient[i][j] = values[offset++];
         }
       }
      //時刻係数
      double T[] = new double[numofcoeff];
      T[0] = 1;
      T[1] = t;
      for (int i = 2; i < numofcoeff; i++)
       {
        T[i] = 2 * t * T[i - 1] - T[i - 2];
       }

      //計算
      double X= 0;
      for (int i = 0; i < numofcoeff ; i++){ X +=( coefficient[0][i] * T[i]); }
      System.out.println("X= "+ X);
      return X;
     }
    else
     {
      System.out.println("Error + "+ start + ":" + finish + " :: " + jd);
     }
    return 0;
   }


  private static double getJuliusDay(double year, double month, double day, double hour, double minute, double second)
   {
    if(month < 3.0)
     {
      month += 12;
      year -= 1;
     }
    double a = Math.floor(year / 100);
    double b = 2 - a + Math.floor(a / 4);

    double JD = Math.floor(365.25 * year) + Math.floor(30.6001 *(month + 1))+ day + b + 1720994.5 + hour /24.0 + minute / 1440 + second / 86400;
    if(2299160.5 > JD) JD = JD - b;

    return JD;
   }

  private static void test()
   {
    try
     {
      BufferedReader reader = new BufferedReader(new FileReader(new File("ascp1680.405")));

      int lines = NCOEFF / 3;
      lines +=  (NCOEFF  % 3) > 0 ? 1 : 0;
      System.out.println("lines = " + lines);
      double last = 2334640.5;
      double startday = 0;
      double finalday = 0;

      for (int i = 0; i < 230;i++)
       {
        //読み込みを開始
        reader.readLine(); //インデックスの行をスキップ

        String str = reader.readLine().trim();
        int blank = str.indexOf(" ");

        String strval = str.substring(0, blank).replace("D", "E");
        startday = Double.parseDouble(strval);
        str = str.substring(blank, str.length()).trim();

        blank = str.indexOf(" ");
        strval = str.substring(0, blank).replace("D", "E");
        finalday = Double.parseDouble(strval);
        System.out.println("difference = " + (startday - last));

        last = startday;
        for(int j = 0; j < (lines - 1); j++) reader.readLine();
       }

     }
    catch(Exception e){}
   }
 }
