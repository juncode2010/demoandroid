package net.juncode.demo.calendarprovider;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by rentony on 17/7/30.
 */

public class CalendarProviderUtils {

    private static String calanderURL = "content://com.android.calendar/calendars";
    private static String calanderEventURL = "content://com.android.calendar/events";
    private static String calanderRemiderURL = "content://com.android.calendar/reminders";

    /**
     * 根据姓名生成事件
     * @param name
     * @return
     */
    public static String generateTitle(String name){
        return name+"该打疫苗了";
    }
    /**
     * 批量添加打事件到日历
     */
    public static void addVaccinateEvent(Context context, String babyName, List<Long> dateList){
        //检查是否已添加
        //title前缀
        String titleHead=generateTitle(babyName);
        //本地已添加的事件
        List<VaccinateEvent> localEventList=read(context,titleHead);
        if(localEventList.size()!=dateList.size()*2){
            List<VaccinateEvent> vaccinateEventList=new LinkedList<>();

            for(Long date:dateList){
                DateFormat datetimeDf = new SimpleDateFormat("yyyy-MM-dd");
                String format = datetimeDf.format(date);
                Log.i("CalendarProviderUtils",format+date);
                // 当天提醒一遍
                VaccinateEvent event=new VaccinateEvent();
                event.date=date;
                event.title=titleHead;
                vaccinateEventList.add(event);
                // 24小时之前提醒一遍
                VaccinateEvent event2=new VaccinateEvent();
                event2.date=date-3600*24*1000;
                event2.title=titleHead;
                vaccinateEventList.add(event2);

            }

            //移除已添加的事件
            vaccinateEventList.removeAll(localEventList);
            //批量添加未添加的事件
            for(VaccinateEvent event:vaccinateEventList){
                Log.i(CalendarProviderUtils.class.getSimpleName(),"向日历中插入："+event);
                add(context,event);
            }
        }else{
            Log.i(CalendarProviderUtils.class.getSimpleName(),"本地日历已存在事件，不必重复添加");
        }

    }

    /**
     * 批量从日历中移除事件
     * @param context
     * @param babyName
     */
    public static void deleteVaccinateEvent(Context context,String babyName){
        //title前缀
        String titleHead=generateTitle(babyName);
        delete(context,titleHead);
    }

    /**
     * 添加事件
     */
    public static void add(Context context, VaccinateEvent vaccinateEvent){
        // 获取要出入的gmail账户的id
        String calId = "";
        Cursor userCursor = context.getContentResolver().query(Uri.parse(calanderURL), null, null, null, null);
        if (userCursor.getCount() > 0) {
            userCursor.moveToLast();  //注意：是向最后一个账户添加，开发者可以根据需要改变添加事件 的账户
            calId = userCursor.getString(userCursor.getColumnIndex("_id"));
        }
        else {
            Log.i(CalendarProviderUtils.class.getSimpleName(),"没有账户，请先添加账户");
            return;
        }

        ContentValues event = new ContentValues();
        event.put("title", vaccinateEvent.title);
//        event.put("description", "");
        // 插入账户
        event.put("calendar_id", calId);
//        System.out.println("calId: " + calId);
//        event.put("eventLocation", "地球-华夏");

        event.put("dtstart", vaccinateEvent.date);
        event.put("dtend", vaccinateEvent.date+3600*2);//顺延两个小时
        event.put("hasAlarm", 1);

        event.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Shanghai");  //这个是时区，必须有，
        //添加事件
        Uri newEvent = context.getContentResolver().insert(Uri.parse(calanderEventURL), event);
        //事件提醒的设定
        long id = Long.parseLong(newEvent.getLastPathSegment());
        ContentValues values = new ContentValues();
        values.put("event_id", id);
        // 提前10分钟有提醒
        values.put("minutes", 10);
        context.getContentResolver().insert(Uri.parse(calanderRemiderURL), values);
        Log.i(CalendarProviderUtils.class.getSimpleName(),"插入事件成功");

    }

    /**
     * 读取指定title的宝宝事件
     * @param context
     */
    public static List<VaccinateEvent> read(Context context,String babyName){
        String title=generateTitle(babyName);
        List<VaccinateEvent> eventList =new LinkedList<VaccinateEvent>();
        Cursor eventCursor = context.getContentResolver().query(Uri.parse(calanderEventURL), null, "title" + " = " + "'"+title+"'", null, null);
        Log.i("事件数量：",eventCursor.getCount()+"");
        if (eventCursor.getCount() > 0) {
            while(eventCursor.moveToNext()){
                String eventTitle = eventCursor.getString(eventCursor.getColumnIndex("title"));
                Long eventDtstart = eventCursor.getLong(eventCursor.getColumnIndex("dtstart"));
                VaccinateEvent event=new VaccinateEvent();
                event.title=eventTitle;
                event.date=eventDtstart;
                eventList.add(event);
//                Log.i("CalendarUtil",eventTitle);
            }
        }
        return eventList;
    }

    /**
     * 删除指定title的事件
     * @param context
     */
    public static void delete(Context context,String title){
        int rownum = context.getContentResolver().delete(Uri.parse(calanderEventURL), "title" + " = " + "'"+title+"'", null);
        //可以令_id=你添加账户的id，以此删除你添加的账户
//        Toast.makeText(context, "删除了: " + rownum, Toast.LENGTH_LONG).show();
    }

    /**
     * 添加账户
     */
    public static String initCalendars(Context context) {

        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, "yy");

        value.put(CalendarContract.Calendars.ACCOUNT_NAME, "mygmailaddress@gmail.com");
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, "com.android.exchange");
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "mytt");
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, -9206951);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, "mygmailaddress@gmail.com");
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri calendarUri = CalendarContract.Calendars.CONTENT_URI;
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, "mygmailaddress@gmail.com")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, "com.android.exchange")
                .build();

        context.getContentResolver().insert(calendarUri, value);

        return "添加账户:mygmailaddress@gmail.com";
    }

    /**
     * 读取账户
     * @param context
     * @return
     */
    public static String readUser(Context context){
        String result="";
        Cursor userCursor = context.getContentResolver().query(Uri.parse(calanderURL), null, null, null, null);

        System.out.println("Count: " + userCursor.getCount());
//        Toast.makeText(context, "Count: " + userCursor.getCount(), Toast.LENGTH_LONG).show();

        for (userCursor.moveToFirst(); !userCursor.isAfterLast(); userCursor.moveToNext()) {
            System.out.println("name: " + userCursor.getString(userCursor.getColumnIndex("ACCOUNT_NAME")));


            String userName1 = userCursor.getString(userCursor.getColumnIndex("name"));
            String userName0 = userCursor.getString(userCursor.getColumnIndex("ACCOUNT_NAME"));
//            Toast.makeText(this, "NAME: " + userName1 + " -- ACCOUNT_NAME: " + userName0, Toast.LENGTH_LONG).show();
            result+="NAME: " + userName1 + " -- ACCOUNT_NAME: " + userName0;
        }
        if(TextUtils.isEmpty(result))
            result="没有账户,插入事件前,需先添加账户";
        return result;
    }

    public static void checkPermission(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try{
                int checkOp = appOpsManager.checkOp(AppOpsManager.OPSTR_RECORD_AUDIO, Binder.getCallingUid(),context.getPackageName());
                Log.i(CalendarProviderUtils.class.getSimpleName(),"checkOp:"+checkOp);
                switch (checkOp){
                    case AppOpsManager.MODE_ALLOWED:
                        //有权限
                        break;
                    case AppOpsManager.MODE_IGNORED:
                        //没有权限
//                        res.invoke("denied","error");
                        return;
                    case AppOpsManager.MODE_ERRORED:
                        //出错了
//                        res.invoke("denied","error");
                        return;
                    case 4:
                        //权限需要询问
//          res.invoke("权限需要询问");
                        break;
                }
            }catch (Exception e){
                Log.i(CalendarProviderUtils.class.getSimpleName(),e.getMessage());
            }
        }
    }

    public static void checkPermission2(Activity context){
        // 申请Dangerous Permissions，包括读写日历、录制录音
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED)
            {
                Log.i("MainActivity","没有访问日历的权限，进行申请");

                ActivityCompat.requestPermissions(context,
                        new String[]{Manifest.permission.WRITE_CALENDAR,Manifest.permission.RECORD_AUDIO},
                        1);
            }else{
                Log.i("MainActivity","有访问日历的权限");
            }
        }
    }

}

