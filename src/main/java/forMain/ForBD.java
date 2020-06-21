package forMain;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ForBD {

    private SessionFactory sessionFactory;
    private ArrayList<Double> arrayToDay = new ArrayList<>();
    private ArrayList<Double> arrayLastWeek = new ArrayList<>();
    private ArrayList<Object> arrayList = new ArrayList<Object>();
    private TreeSet<String> treeSet = new TreeSet<>();
    private boolean pusto = true;
    private int r = 0;
    private StringJoiner joiner;

    public ForBD() {
        sessionFactory = HibernateUtil.getSessionFactory();
    }

    public void getCompany() {
        Session session = sessionFactory.openSession();

        Calendar calendar = new GregorianCalendar();
        String dToDay = new SimpleDateFormat("d").format(calendar.getTime());  // подготавливаем текущую дату для запроса в БД
        String mToDay = new SimpleDateFormat("MM").format(calendar.getTime());  // подготавливаем текущую дату для запроса в БД
        String fToDay = dToDay + "." + mToDay;

        calendar.add(Calendar.DAY_OF_MONTH, -7);  // подготавливаем старую дату для запроса в БД
        String dLastWeek = new SimpleDateFormat("d").format(calendar.getTime());  // подготавливаем старую дату для запроса в БД
        String mLastWeek = new SimpleDateFormat("MM").format(calendar.getTime());  // подготавливаем старую дату для запроса в БД
        String fLastWeek = dLastWeek + "." + mLastWeek;

        Query query = session.createNativeQuery("SELECT DISTINCT first.name FROM (SELECT name, date FROM stock WHERE date = " + fLastWeek + ") AS first LEFT JOIN (SELECT name, date FROM stock WHERE date = " + fToDay + ") AS second ON first.name = second.name WHERE first.date = " + fLastWeek + " AND second.date = " + fToDay);  // забираем все уникальные названия кампаний из БД
        arrayList = (ArrayList<Object>) query.getResultList();

        for (Object o : arrayList) {  // забираем стоимость за сегодня и 7 дней назад
            arrayToDay.add(session.createNativeQuery("SELECT * FROM stock WHERE name = " + "'" + o + "'" +
                    " AND date = " + fToDay, Stock.class).getSingleResult().getCost());
            arrayLastWeek.add(session.createNativeQuery("SELECT * FROM stock where name = " + "'" + o + "'" +
                    " AND date = " + fLastWeek, Stock.class).getSingleResult().getCost());

        }

        session.close();

        for (int i = 0; i < arrayList.size(); i++) {
            if ((arrayToDay.get(i) / arrayLastWeek.get(i) * 100) > 115 || (arrayToDay.get(i) / arrayLastWeek.get(i) * 100) < 85) {
                treeSet.add((String) arrayList.get(i));
                pusto = false;
            }
        }

        joiner = new StringJoiner("%0A");
        Iterator iterator = treeSet.iterator();

        while (iterator.hasNext()) {
            joiner.add((String) iterator.next());
            r++;
            if (r == 7) {  // разбиваем смски по 7 кампаний за раз
                ChatBot.sendMassage(joiner.toString());
                r = 0;
                joiner = new StringJoiner("%0A");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    ChatBot.sendMassage("Сбой на трэдслипе");
                }
            }
            if (!iterator.hasNext()) {
                ChatBot.sendMassage(joiner.toString());
            }
        }

        if (pusto) {
            ChatBot.sendMassage("Сегодня пусто");
        }


    }

}
