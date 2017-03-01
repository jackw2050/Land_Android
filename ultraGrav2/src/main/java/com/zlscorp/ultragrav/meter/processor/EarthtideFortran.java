package com.zlscorp.ultragrav.meter.processor;

import android.location.Location;

public class EarthtideFortran {

	// ****************** TIDE CORRECTION COMPUTATION ROUTINE **********
	// positive north and west
	// Compliments Geological Survey Canada
	// Fudge factor 1.17

	private double earthtide, century, century2, days;
	private double s, p, h, ps, es, oln, soln, ci, si, sn, cn, tit, et, olm, ha, chi, al, ct;
	private double da, dr, gm, ols, chis, ds, cf, gs;
	private int[] firstDayOfMonth = { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 };
	private final double radiansPerDegree = 0.0174532925199;
	private int leap;

	public EarthtideFortran() {
	}

	public double calculate(ZlsTime myTime, Location myLocation) {

		if (Math.abs(myLocation.getLatitude()) > 90. || Math.abs(myLocation.getLongitude()) > 180.) {
			earthtide = 0.;
		} else {
			days = myTime.getDecimalTime() / 24 + myTime.monthDay - 1 + firstDayOfMonth[myTime.month];
			leap = myTime.year / 4;

			if ((leap * 4 == myTime.year) && (myTime.month < 3)) {
				leap--;
			}
			century = (myTime.year * 365 + leap + days + .5) / 36525;
			century2 = century * century;
			s = 4.720023434e0 + 8.399709299e3 * century + 4.40696e-5 * century2;
			p = 5.835124713e0 + 7.101800936e1 * century - 1.80545e-4 * century2 - 2.1817e-7 * century * century2;
			h = 4.88162792e0 + 6.283319509e2 * century + 5.27962e-6 * century2;
			oln = 4.523588564e0 - 3.375715303e1 * century + 3.6749e-5 * century2;
			ps = 4.908229461e0 + 3.000526416e-2 * century + 7.902463e-6 * century2;
			es = 1.675104e-2 - 4.180e-5 * century - 1.26e-7 * century2;
			soln = Math.sin(oln);
			ci = .91369 - .03569 * Math.sin(oln);
			si = Math.sqrt(1. - ci * ci);
			sn = .08968 * soln / si;
			cn = Math.sqrt(1. - sn * sn);
			tit = .39798 * soln / (si * (1. + Math.cos(oln) * cn + .91739 * soln * sn));
			et = 2 * Math.atan(tit);
			if (et < 0.) {
				et = et + 6.2831852;
			}
			olm = s - oln + et + 1.0979944e-1 * Math.sin(s - p) + 3.767474e-3 * Math.sin(2. * (s - p)) + 1.54002e-2 * Math.sin(s - 2. * h + p) + 7.69395e-3
					* Math.sin(2. * (s - h));
			ha = (15. * myTime.getDecimalTime() - 180. - myLocation.getLongitude()) * radiansPerDegree;
			chi = ha + h - Math.atan(sn / cn);
			al = myLocation.getLatitude() * radiansPerDegree;
			ct = Math.sin(al) * si * Math.sin(olm) + Math.cos(al) * ((1. + ci) * Math.cos(olm - chi) + (1. - ci) * Math.cos(olm + chi)) / 2.;
			da = 2.60144 + .143250 * Math.cos(s - p) + .0078644 * Math.cos(2. * (s - p)) + .0200918 * Math.cos(s - 2. * h + p) + .0146006 * Math.cos(2. * (s - h));
			dr = 6.378388 / Math.sqrt(1. + 6.76902e-3 * (1. - Math.pow(Math.cos(al), 2)));
			gm = .49049 * dr * da * da * da * (3 * ct * ct - 1.) + .00074 * dr * dr * Math.pow(da, 4) * ct * (5. * ct * ct - 3.);
			ols = h + 2. * es * Math.sin(h - ps);
			chis = ha + h;
			ds = .668881 * (1. + es * Math.cos(h - ps)) / (1. - es * es);
			cf = .39798 * Math.sin(al) * Math.sin(ols) + Math.cos(al) * (.95869 * Math.cos(ols - chis) + .0413 * Math.cos(ols + chis));
			gs = 13.2916 * dr * (3. * cf * cf - 1.) * ds * ds * ds;
			earthtide = (gm + gs) * .00117;
		}
		return earthtide;
	}
}
