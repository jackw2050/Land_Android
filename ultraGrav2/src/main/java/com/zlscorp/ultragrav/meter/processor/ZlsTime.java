package com.zlscorp.ultragrav.meter.processor;

import android.text.format.Time;

public class ZlsTime extends Time {

  /**
   * Calculates decimal hours.
   * @return hours, in decimal (i.e. 1:15PM = 13.25)
   */
  public double getDecimalTime () {
    return (this.hour + this.minute/60. + this.second/3600.);
  }
}
