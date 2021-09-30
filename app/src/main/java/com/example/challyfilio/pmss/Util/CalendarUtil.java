package com.example.challyfilio.pmss.Util;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CalendarUtil {
    private static String CALENDER_URL = "content://com.android.calendar/calendars";
    private static String CALENDER_EVENT_URL = "content://com.android.calendar/events";
    private static String CALENDER_REMINDER_URL = "content://com.android.calendar/reminders";

    private static String CALENDARS_NAME = "challyfilio";
    private static String CALENDARS_ACCOUNT_NAME = "challyfilio4368@gmail.com";
    private static String CALENDARS_ACCOUNT_TYPE = "com.android.challyfilio";
    private static String CALENDARS_DISPLAY_NAME = "account";

    //检查是否已经添加了日历账户
    private static int checkAndAddCalendarAccount(Context context) {
        int oldId = checkCalendarAccount(context);
        if (oldId >= 0) {
            return oldId;//存在账户，返回id
        } else {
            long addId = addCalendarAccount(context);//添加账户
            if (addId >= 0) {
                return checkCalendarAccount(context);//成功返回账户id
            } else {
                return -1;//失败返回-1
            }
        }
    }

    //检查是否存在现有账户，存在则返回账户id，否则返回-1
    private static int checkCalendarAccount(Context context) {
        Cursor userCursor = context.getContentResolver().query(Uri.parse(CALENDER_URL), null, null, null, null);
        try {
            if (userCursor == null) { //查询返回空值
                return -1;
            }
            int count = userCursor.getCount();
            if (count > 0) { //存在现有账户，取第一个账户的id返回
                userCursor.moveToFirst();
                return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
            } else {//不存在，返回-1
                return -1;
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
    }

    //添加日历账户，账户创建成功则返回账户id，否则返回-1
    private static long addCalendarAccount(Context context) {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, CALENDARS_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
        value.put(CalendarContract.Calendars.VISIBLE, 1);//选中日历是否要被展示
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);//账户级别
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);//是否同步和是否保存到设备
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri calendarUri = Uri.parse(CALENDER_URL);
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
                .build();

        Uri result = context.getContentResolver().insert(calendarUri, value);
        long id = result == null ? -1 : ContentUris.parseId(result);
        return id;
    }

    //添加日程事件
    public static void addCalendarEvent(Context context, String title, String description, String str_date) {
        if (context == null) {
            return;
        }
        int calId = checkAndAddCalendarAccount(context); //获取日历账户的id
        if (calId < 0) { //获取账户id失败直接返回
            Toast.makeText(context, "添加日程失败", Toast.LENGTH_SHORT).show();
            return;
        }

        //添加日程
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        long start = 0;
        try {
            date = sdf.parse(str_date);
            Log.e("CUee2", str_date);
            start = date.getTime();//设置开始时间
            Log.e("CUee", "日程开始时间:" + start);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(start + 9 * 60 * 60 * 1000);//设置终止时间，开始时间加9小时
        long end = mCalendar.getTime().getTime();

        ContentValues event = new ContentValues();
        event.put("title", title);
        event.put("description", description);
        event.put("calendar_id", calId); //插入账户的id
        event.put(CalendarContract.Events.DTSTART, start);
        event.put(CalendarContract.Events.DTEND, end);
        event.put(CalendarContract.Events.HAS_ALARM, 1);//设置有闹钟提醒
        event.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Beijing");//这个是时区，必须有
        Uri newEvent = context.getContentResolver().insert(Uri.parse(CALENDER_EVENT_URL), event); //添加事件

        if (newEvent == null) { //添加日历事件失败直接返回
            Toast.makeText(context, "日程添加失败", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(context, "日程添加成功", Toast.LENGTH_SHORT).show();
        }
    }
}