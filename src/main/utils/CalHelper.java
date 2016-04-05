package main.utils;

import main.data.Tags;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class CalHelper {

    public static final TimeZone TIME_ZONE_GMT = TimeZone.getTimeZone("GMT");

    public static Calendar getCalendar(String dateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(Tags.SD_FORMAT);
        sdf.setTimeZone(TIME_ZONE_GMT);

        Calendar cal = Calendar.getInstance(TIME_ZONE_GMT);
        cal.setTime(sdf.parse(dateString));
        return cal;
    }

    public static String getStringDate(Calendar calendar) {
        SimpleDateFormat dFormat = new SimpleDateFormat(Tags.SD_FORMAT); /*set the date format*/
        if (!dFormat.getTimeZone().equals(TIME_ZONE_GMT))
            dFormat.setTimeZone(TIME_ZONE_GMT);
        //if (!calendar.getTimeZone().equals(TIME_ZONE_GMT))
            //calendar.setTimeZone(TIME_ZONE_GMT);
        return dFormat.format(calendar.getTime());
    }
}
