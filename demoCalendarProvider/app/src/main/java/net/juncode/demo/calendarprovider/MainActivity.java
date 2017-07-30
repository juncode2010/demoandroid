package net.juncode.demo.calendarprovider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.readUserButton) {  //读取日历账户
            String result = CalendarProviderUtils.readUser(this);
            Toast.makeText(this,result,Toast.LENGTH_LONG).show();
        }
        else if (v.getId() == R.id.inputaccount) { //添加日历账户
            String result = CalendarProviderUtils.initCalendars(this);
            Toast.makeText(this,result,Toast.LENGTH_LONG).show();
        }
        else if (v.getId() == R.id.delEventButton) {  //删除事件
            CalendarProviderUtils.deleteVaccinateEvent(this,"doudou");
            Toast.makeText(this,"批量删除事件成功",Toast.LENGTH_LONG).show();
        }
        else if (v.getId() == R.id.readEventButton) {  //读取事件
            List<VaccinateEvent> eventList = CalendarProviderUtils.read(this, "doudou");
            Toast.makeText(this,eventList.toString(),Toast.LENGTH_LONG).show();
        }
        else if (v.getId() == R.id.writeEventButton) {
            writeEventButton();
            Toast.makeText(this,"批量写入事件成功",Toast.LENGTH_LONG).show();
        }
    }

    public void writeEventButton() {
        List<Long> dateList=new ArrayList<Long>();
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.HOUR_OF_DAY, 11);
        mCalendar.set(Calendar.MINUTE, 45);
        long now = mCalendar.getTime().getTime();
        for(int i=0;i<7;i++){
            Log.i(MainActivity.class.getSimpleName(),now+"");
            dateList.add(now);
            now+=(3600*24*7*1000);
        }
        CalendarProviderUtils.addVaccinateEvent(this,"doudou",dateList);
    }

}
