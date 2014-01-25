package com.carnegiemellonracing.racetimer;

public class Utils {
  public Utils () {
  }
  
  public static String parseTime(long t) {
    int secs = (int) (t / 1000);
    int mins = secs / 60;
    secs = secs % 60;
    int hrs = mins / 60;
    mins = mins % 60;
    int milliseconds = (int) (t % 1000);
    String out = ""+ String.format("%02d", mins) + ":"
                  + String.format("%02d", secs) + "."
                  + String.format("%03d", milliseconds);
    if (hrs > 0) {
      out = "" + hrs + ":" + out;
    }
    return out;
  }
}
