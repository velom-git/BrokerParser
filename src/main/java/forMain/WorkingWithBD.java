package forMain;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@SuppressWarnings("ALL")
public class WorkingWithBD {

    private SessionFactory sessionFactory;
    private Session session;
    private ArrayList<Double> arrayToDay = new ArrayList<>();
    private ArrayList<Double> arrayLastWeek = new ArrayList<>();
    private List<String> arrayList = new ArrayList<>();
    private TreeSet<String> treeSet = new TreeSet<>();
    private boolean isEmpty = true;
    private int atATime = 0;
    private StringJoiner joiner;
    private Calendar calendar = new GregorianCalendar();

    public void getCompaniesNames() {

        String dToDay = new SimpleDateFormat("d").format(calendar.getTime());  // подготавливаем текущую дату для запроса в БД
        String mToDay = new SimpleDateFormat("MM").format(calendar.getTime());  // подготавливаем текущую дату для запроса в БД
        String fToDay = dToDay + "." + mToDay;

        calendar.add(Calendar.DAY_OF_MONTH, -7);  // подготавливаем старую дату для запроса в БД
        String dLastWeek = new SimpleDateFormat("d").format(calendar.getTime());  // подготавливаем старую дату для запроса в БД
        String mLastWeek = new SimpleDateFormat("MM").format(calendar.getTime());  // подготавливаем старую дату для запроса в БД
        String fLastWeek = dLastWeek + "." + mLastWeek;

        sessionFactory = HibernateUtil.getSessionFactory();
        session = sessionFactory.openSession();

        Query query = session.createNativeQuery("SELECT DISTINCT first.name FROM (SELECT name, date FROM stock WHERE date = "
                + fLastWeek + ") AS first JOIN (SELECT name, date FROM stock WHERE date = " + fToDay +
                ") AS second ON first.name = second.name WHERE first.date = " + fLastWeek + " AND second.date = " + fToDay);  // забираем все уникальные названия кампаний из БД
        arrayList = (ArrayList<String>) query.getResultList();

        for (String o : arrayList) {  // забираем стоимость за сегодня и 7 дней назад
            arrayToDay.add(session.createNativeQuery("SELECT * FROM stock WHERE name = " + "'" + o + "'" +
                    " AND date = " + fToDay, Stock.class).getSingleResult().getCost());
            arrayLastWeek.add(session.createNativeQuery("SELECT * FROM stock where name = " + "'" + o + "'" +
                    " AND date = " + fLastWeek, Stock.class).getSingleResult().getCost());
        }

        session.close();

    }

    void mathAndSend() {

        for (int i = 0; i < arrayList.size(); i++) {
            if ((arrayToDay.get(i) / arrayLastWeek.get(i) * 100) > 115 || (arrayToDay.get(i) / arrayLastWeek.get(i) * 100) < 85) {
                treeSet.add(arrayList.get(i));
                isEmpty = false;
            }
        }  // находим кампании, дельта которых составляет +-15%

        joiner = new StringJoiner("%0A");  //перевод строки в url кодировке
        Iterator<String> iterator = treeSet.iterator();

        while (iterator.hasNext()) {
            joiner.add(iterator.next());
            atATime++;
            if (atATime == 7) {  // разбиваем смски по 7 кампаний за раз
                ChatBot.sendMassage(joiner.toString());
                atATime = 0;
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

        if (isEmpty) {
            ChatBot.sendMassage("Сегодня пусто");
        }

    }
}
