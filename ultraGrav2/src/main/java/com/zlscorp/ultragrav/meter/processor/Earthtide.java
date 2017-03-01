package com.zlscorp.ultragrav.meter.processor;

import android.location.Location;
import android.text.format.Time;

//import java.util.Calendar;
//import java.util.GregorianCalendar;

/**
 * Computes the tidal acceleration due to the moon and the sun
 * 
 * 2002/05/06 modified to output mGals 
 * 
 * 2005/07/11 We verified that the output is ET correction <<<<< NOTE <<<<<<
 * 
 * Reference: Longman, i.m., 1959. Formulas for computing the tidal accelerations due to 
 * the moon and the sun Journal of Geophysical Research Vol 64, No 12, December 1959 page 
 * 2351 to 2355.
 * 
 * latitude = terrestrial latitude of the location on the earth's surface, measured in decimal degrees. 
 *     + for northern hemisphere 
 *     - for southern hemisphere 
 * longitude = terrestrial longitude of the location on the earth's surface, measured in decimal degrees. 
 *     + for east of prime meridian 
 *     - for west of prime meridian 
 * elev = height of the location above sea level, in meters. 
 * gmt = greenwich mean time in decimal hours (2400 hour clock) eg 13.25 for 13hrs 15mins (1:15pm) 
 * day = the day of the observation, int 1 to 31 
 * month = the month of the observation, int 1 to 12 
 * year = the year of the observation, specified as a four digit int e.g. 1992
 * 
 */

public class Earthtide {

	private final double EM = 0.054900489;
	private final double EM2 = EM * EM;
	private final double A = 6.37816e6;
	private final double ONE = 1.;
	private final double TWO = 2.;
	private final double THREE = 3.;
	private final double MU = 6.672e-11;
	private final double MM = 7.3537e22;
	private final double MS = 1.993e30;
	private final double UMM = MU * MM;
	private final double UMS = MU * MS;
	private final double M = 0.0748041;
	private final double M2 = M * M;
	private final double CS = 1.495e11;
	private final double CM = 3.84402e8;
	private final double SIEC = 0.089676558;
	private final double CIEC = 0.995970941;
	private final double REV = 1.296e6;
	private final double LOVE = 120000.;           // 1.2 * 100000
	private final double SW = 0.397980655;
	private final double CW = 0.917393808;
	private final double W2 = 0.204657308;

	private double calculated;
//	public boolean use;

	public Earthtide() {
	}
	
//  public double calculate(Time time, Location myLocation) {

	public double calculate(long myLongTime, Location myLocation) {

//	    Calendar myCalTime = GregorianCalendar.getInstance();
//	    myCalTime.setTimeInMillis(myLongTime);

	    Time myTime = new Time("UTC");
	    myTime.set(myLongTime);
	    myTime.isDst = 0;

		double cl, sl, r, ai, t, t2, t3, s, p, n, h, smp, d, ieq2, cieq;
		double sieq, v, sn, sa, ca, alpha, sigma, l, rasc, ct, ct2;
		double latr, ee, tt, gm, gs, smh, smh2, smp2, shp2;

		/*
		 * units contain the scaling factors for the required gravity units. love is the love number 1.20 for an elastic earth. rev is seconds in 360 degrees.
		 * 
		 * constant em has been updated, the equatorial radius and eccentricity**2 of the earth have also been updated to the grs 1967 spheroid, see melchoir, p., 'the tides of the
		 * planet earth', 2nd edition.
		 * 
		 * compute number of julian centuries since 12.00 hours on Dec, 31 1899 year = Y2K::AnyYearToYYYY(year); Make sure it is in yyyy format
		 * 
		 * Coordinate (0,0) is not possible for a land meter and therefore is used to indicate that no coordinates are available .
		 */

		if (myLocation.getLatitude() == 0. & myLocation.getLongitude() == 0.) {
			calculated = 0.;
		} else {
			t = julianCenturies(myTime);
			t2 = t * t;
			t3 = t2 * t;

			// compute acceleration due to the moon.
			s = secondsToRadians(973574.72 + (1336. * REV + 1108411.2) * t + 9.09 * t2 + 0.0068 * t3);
			p = secondsToRadians(1203580.87 + (11. * REV + 392515.94) * t - 37.24 * t2 - 0.045 * t3);
			n = secondsToRadians(933057.12 - (5.0 * REV + 482912.63) * t + 7.58 * t2 + 0.008 * t3);
			h = secondsToRadians(1006908.04 + 129602768.13 * t + 1.089 * t2);
			smp = s - p;
			smp2 = TWO * smp;
			smh = s - h;
			smh2 = TWO * smh;
			shp2 = s - TWO * h + p;
			cieq = CW * CIEC - SW * SIEC * Math.cos(n);
			sieq = Math.sqrt(ONE - cieq * cieq);
			ieq2 = 0.5 * Math.asin(sieq);
			sn = Math.sin(n);
			v = Math.asin(SIEC * sn / sieq);
			sa = SW * sn / sieq;
			ca = Math.cos(n) * Math.cos(v) + sn * Math.sin(v) * CW;
			alpha = TWO * Math.atan2(sa, ONE + ca);
			sigma = s - (n - alpha);
			l = sigma + TWO * EM * Math.sin(smp) + 1.25 * EM2 * Math.sin(smp2) + 3.75 * M * EM * Math.sin(shp2) + 1.375 * M2 * Math.sin(smh2);

			// positive west
			// tt = degrads (15. * (gmt-12.) - llong);
			// positive east

			tt = degreesToRadians(15. * (getDecimalTime(myTime) - 12.) + myLocation.getLongitude());
			rasc = tt + h - v;
			ai = ONE / (CM * (ONE - EM2));
			d = ONE / CM + ai * EM * Math.cos(smp) + ai * EM2 * Math.cos(smp2) + 1.875 * ai * M * EM * Math.cos(shp2) + ai * M2 * Math.cos(smh2);
			latr = degreesToRadians(myLocation.getLatitude());
			sl = Math.sin(latr);
			cl = Math.cos(latr);
			r = Math.sqrt(ONE / (ONE + 0.6694605328569e-2 * sl * sl)) * A + myLocation.getAltitude(); // Altitude is a stand in for
																										// elevation
			ct = sl * sieq * Math.sin(l) + cl * (Math.cos(ieq2) * Math.cos(ieq2) * Math.cos(l - rasc) + Math.sin(ieq2) * Math.sin(ieq2) * Math.cos(l + rasc));
			ct2 = ct * ct;
			gm = UMM * r * d * d * d * ((THREE * ct2 - ONE) + 1.5 * r * d * (5. * ct2 * ct - THREE * ct));

			// compute acceleration due to the sun.
			p = secondsToRadians(1012395. + 6189.03 * t + 1.63 * t2 + 0.012 * t3);
			ee = 0.01675104 - 0.418e-4 * t - 0.126e-6 * t2;
			l = h + TWO * ee * Math.sin(h - p);
			rasc = tt + h;
			ai = ONE / (CS * (ONE - ee * ee));
			d = ONE / CS + ai * ee * Math.cos(h - p);
			ct = sl * SW * Math.sin(l) + cl * (Math.cos(W2) * Math.cos(W2) * Math.cos(l - rasc) + Math.sin(W2) * Math.sin(W2) * Math.cos(l + rasc));
			gs = UMS * r * d * d * d * (THREE * ct * ct - ONE);

			// compute total acceleration due to moon and sun.
			// include the love number correction and scale to requested gravity units
			calculated = (gm + gs) * LOVE;       // was LOVE * 100000, where LOVE = 1.2. Now LOVE = 120000.
		}
        return calculated;
	}

    private double getDecimalTime (Time time) {
        return (time.hour + time.minute/60. + time.second/3600.);
//        return (time.get(Calendar.HOUR) + time.get(Calendar.MINUTE)/60. + time.get(Calendar.SECOND)/3600.);
    }

	/**
	 * Computes the number of Julian centuries from 12.00 hours (noon?) on 31st December, 1899.
	 * 
	 * Modified from: Numerical Recipes by press, w.h, flannery, b.p., teukolsky, s.a. and vetterling, w.t. Cambridge university press, 1989.
	 * 
	 * date = 1 to 31 month = 1 to 12 year = 4 digit year, ex. 2012 greenwichMeanTime = decimal hours in 24 hour clock, ex 1:15PM = 13.25
	 */
	private double julianCenturies(Time myTime) {
		long julianYear; 
		long julianMonth;
        long julianDay;
		long ja;
		
		myTime.month++;      // Convert value returned as 0-11 to 1-12.
//		date.set(Calendar.MONTH, date.get(Calendar.MONTH));

		// debug variables
//		double jY, jM, jA, result;

		if (myTime.month > 2) {
			julianYear = myTime.year;
			julianMonth = myTime.month + 1;
		} else {
			julianYear = myTime.year - 1;
			julianMonth = myTime.month + 13;
		}

		// debug
//		jY = (double) julianYear;
//		jY = 365.25 * (double) julianYear;
//		julianDay = (long) (365.25 * (double) julianYear);
//		jM = (double) julianMonth;
//		jM = 30.6001 * (double) julianMonth;
//		julianDay = (long) (30.6001 * (double) julianMonth);
//		jA = 0.01 * julianYear;
		// debug

		julianDay = (long) (365.25 * (double) julianYear) + (long) (30.6001 * (double) julianMonth) + myTime.monthDay + 1720995;
		ja = (long) (0.01 * julianYear);

		// 12.00 hours on 31st December 1899 is julian day number 2415020
		// result = ((double) (julianDay + 2 - ja + (long) (0.25 * ja)) + (myTime.getTimeinDecimal() / 24.) - 0.5 - 2415020.) /
		// 36525.0;
		return ((double) (julianDay + 2 - ja + (long) (0.25 * ja)) + (getDecimalTime(myTime) / 24.) - 0.5 - 2415020.) / 36525.0;
	}

	private double secondsToRadians(double a) {
		// TODO - add error checking?
		return (a * 4.8481368110953e-6);
	}

	private double degreesToRadians(double a) {
		// TODO - add error checking?
		return (a * 0.0174532951994329);
	}
}
