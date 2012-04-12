package bigs.core.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import bigs.api.exceptions.BIGSException;


public class Text {

    /**
     * Returns a new string composed of string <b>s</b> appended with empty spaces until reaching length <b>l</b>.
     * If string <b>s</b> contains more than <b>l</b> characters it is trimmed to contain exactly <b>l</b> characters
     * @param s the String to justify
     * @param l the maximum length of the justified String
     * @return the justified string
     */
    public static String leftJustifyExact (String s, int l) {
        return leftJustify(s, l).substring(0, l);
    }

    /**
     * Returns a new string composed of string <b>s</b> appended with empty spaces until reaching length <b>l</b>.
     * If string <b>s</b> contains more than <b>l</b> characters it is left untouched.
     * @param s the String to justify
     * @param l the desired length of the justified String.
     * @return the justified string
     */
    public static String leftJustify(String s, int l) {
        if (s==null) s="null";
        for (int i=s.length(); i< l; i++) s = s + " ";
        return s;
    }
    
    /**
     * Returns a new string composed of string <b>s</b> preppended with the given char until reaching length <b>l</b>.
     * If string <b>s</b> contains more than <b>l</b> characters it is left untouched.
     * @param s the String to justify
     * @param l the desired length of the justified String.
     * @param c the character to prepend to fill in
     * @return the justified string
     */
    public static String rightJustify(String s, int l, char c) {
        if (s==null) s="null";
        for (int i=s.length(); i< l; i++) s = c+s;
        return s;        	
    }
    
    /**
     * Returns a new string composed of string <b>s</b> prepended with empty spaces until reaching length <b>l</b>.
     * If string <b>s</b> contains more than <b>l</b> characters it is left untouched.
     * @param s the String to justify
     * @param l the desired length of the justified String.
     * @return the justified string
     */
    public static String rightJustify(String s, int l) {
    	return rightJustify (s, l, ' ');
    }
    
    /**
     * pads with zeroes a long integer
     * @param i
     * @return
     */
    public static String zeroPad(Long i) {
    	return zeroPad(i, 5);
    }        

    public static String zeroPad(Long i, Integer length) {
    	return Text.rightJustify(i.toString(), length, '0');
    }        

    /**
     * returns a string consisting in a char 'c' repeated 'count' times
     * @param c the char
     * @param count the number of times the char is repeated in the resulting string
     * @return
     */
    public static String charString(String c, int count) {
        StringBuffer sb = new StringBuffer();
        for (int i=1; i<=count; i++) sb.append(c);
        return sb.toString();
    }

    /**
     * removes the first element from the array passed as argument
     * @param s
     * @return
     */
    public static String[] shift (String s[]) {
         return shift(s, 1);
    }

    /**
     * returns an array of string resulting from removing the 'pos' first elements 
     * from the one passed as argument
     * @param s
     * @param pos
     * @return
     */
    public static String[] shift (String s[], int pos) {
         if (pos>=s.length) return new String[0];

         String[] r = new String[s.length-pos];
         for (int i=pos; i<s.length; i++) r[i-pos] = s[i];
         return r;
    }
    
    /**
     * parses a string containing a list of string representations of objects. For instance "10:20:30"
     * to get a list of integer.
     * Example call: <code>List<Integer> l = splitString("10:20:30", ":", Integer.class)</code>
     * @param s the string to parse
     * @param separator a character that separates objects in the input string
     * @param c the class into which objects are returned
     * @return a list of parsed objects
     */
    public static <T> List<T> parseObjectList(String s, String separator, Class<T> c) {
    	
    	String[] tokens = s.split(separator);
    	List<T> result = new ArrayList<T>();
    	for (String token: tokens) {
    		result.add(parseObject(token, c));
    	}
    	return result;
    }
    
    /**
     * parses a string containing the string representation of an object.
     * @param s the string to parse
     * @param c the class into which the parsed object is to be casted into
     * @return the new object
     */
    @SuppressWarnings("unchecked")
	public static <T> T parseObject (String s, Class<T> c) {
    	T obj = null;
		if (c == Double.class)  obj = (T)new Double(s);
		else if (c == Integer.class) obj = (T)new Integer(s);
		else if (c == Boolean.class) obj = (T)new Boolean(s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("1"));
		else if (c == String.class)  obj = (T)s;
		else if (c == Long.class)    obj = (T)new Long(s);
		else throw new BIGSException("parsing params of type "+c.getName()+" is not supported");
    	return obj;
    }
    
    /**
     * collates the strings in the input array using the specified separator
     * @param strings
     * @param separator
     * @return
     */
    public static String collate(String[] strings, String separator) {
    	StringBuffer sb = new StringBuffer();
    	for (int i=0; i<strings.length; i++) {
    		sb.append(strings[i]);
    		if (i!=strings.length) {
    			sb.append(separator);
    		}
    	}
    	return sb.toString();
    }
    
    /**
     * same as before, accepting a list of strings as arguments
     * @param strings
     * @param separator
     * @return
     */
    public static String collate(List<String> strings, String separator) {
    	return collate(strings.toArray(new String[]{}), separator);
    }
    
    /**
     * splits a string following separator marks
     * @param s
     * @param separator
     * @return
     */
    public static String[] splitString (String s, String separator) {
        StringTokenizer st = new StringTokenizer (s, separator);
        List<String> v = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            v.add(st.nextToken());
        }
        return v.toArray(new String[]{});
    }    

    /**
     * returns a human readable format of the milliseconds passed as argument
     * such as 61000 is "1min 1sec"
     * @param time the amount of milliseconds to format as a string
     * @return a string containing human readable format.
     */
    public static String timeToString(long time) {
        DecimalFormat dfmt = new DecimalFormat("####0.00;-####0.00", new DecimalFormatSymbols(new Locale("EN", "US")));
        double t = new Double(time).doubleValue();
        if (Math.abs(t)<1000.0)
            return dfmt.format(t)+"ms ";
        t = t / 1000.0;
        // greater than two hours we display the time in hours
        if (Math.abs(t) > 7200) {
            t = t / 60.0 / 60.0;
            return dfmt.format(t)+"hrs";
        }
        // between 2 hours and 2 minutes we display the time in minutes
        if (Math.abs(t) > 120) {
            t = t / 60.0;
            return dfmt.format(t)+"min";
        }
        // otherwise we display the time in secs
        return dfmt.format(t)+"sec";
    }

    // returns the number of the first line matching the regexp
    public static int firstLineWithRegExp (List<String> vs, String regexp) {
        Pattern p = Pattern.compile(regexp);

        for (int i=0; i< vs.size(); i++) {
            String s = vs.get(i);
            if (p.matcher(s).find()) return i;
        }
        return -1;
    }

    public static boolean matchesRegExp (String s, String regexp) {
        ArrayList<String> v = new ArrayList<String>();
        v.add(s);
        return matchesRegExp(v, regexp);
    }

    public static boolean matchesRegExp (List<String> vs, String regexp) {
        return (firstLineWithRegExp(vs, regexp)!=-1);
    }

}
