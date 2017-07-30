package net.juncode.demo.calendarprovider;

/**
 * Created by yajun on 2017/7/26.
 * 打疫苗事件
 */
public class VaccinateEvent {

    //事件标题
    public String title;

    //接种日期
    public long date;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VaccinateEvent event = (VaccinateEvent) o;

        if (date != event.date) return false;
        return title.equals(event.title);

    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + (int) (date ^ (date >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "VaccinateEvent{" +
                "title='" + title + '\'' +
                ", date=" + date +
                '}';
    }
}
