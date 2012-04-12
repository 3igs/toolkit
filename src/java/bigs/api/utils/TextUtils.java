package bigs.api.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bigs.api.exceptions.BIGSException;


/**
 *
 * @author rlx
 */
public class TextUtils {
		public static DecimalFormat F0    = new DecimalFormat("0;-0", new DecimalFormatSymbols(new Locale("EN", "US")));
        public static DecimalFormat F1    = new DecimalFormat("0.0;-0.0", new DecimalFormatSymbols(new Locale("EN", "US")));
        public static DecimalFormat F2    = new DecimalFormat("0.00;-0.00", new DecimalFormatSymbols(new Locale("EN", "US")));
        public static DecimalFormat F3    = new DecimalFormat("0.000;-0.000", new DecimalFormatSymbols(new Locale("EN", "US")));
        public static DecimalFormat F7    = new DecimalFormat("0.0000000;-0.00000000", new DecimalFormatSymbols(new Locale("EN", "US")));

		public static SimpleDateFormat FULLDATE = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

}
