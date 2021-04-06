package br.ufc.great.greatroom.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by adyson on 02/12/15.
 */
public class DateHelper {

    public static Date fromRFC2822(String rfcDate) {
        String pattern = "EEE, dd MMM yyyy HH:mm:ss Z";
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        try {
            return format.parse(rfcDate);
        } catch (ParseException e) {
            return null;
        }
    }
}
