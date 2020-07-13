package forMain;

import org.hibernate.Session;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class Parser {
    List<String> name = new ArrayList<>();
    List<Double> cost = new ArrayList<>();

    private void getElements(String url, int k) {
        try {
            Document page = Jsoup.connect(url).timeout(4000).get();
            Element table = page.select("table[class=simple-little-table trades-table]").first();
            Elements tr = table.getElementsByTag("tr");  // убираем лишние теги

            for (int i = 0; i < tr.size(); ) {
                if (tr.get(i).text().startsWith(i + 1 + " ")) {
                    i++;
                } else {
                    tr.remove(i);
                }
            }   // чистим от мусора

            Elements td = tr.select("td");  // убираем лишние теги

            for (int i = 2, j = 6; i < td.size(); i += k, j += k) {
                if (!td.get(i).text().isEmpty() & !td.get(j).text().isEmpty()) {
                    name.add(td.get(i).text().replaceAll("'", " "));
                    cost.add(Double.parseDouble(td.get(j).text()));
                }
            }  // забираем название и стоимость

        } catch (IOException e) {
            ChatBot.sendMassage("Ошибка при парсе страницы");
        }

    }

    public void addEntities() {

        String toDay = new SimpleDateFormat("d.MM").format(new Date());  // получаем сегодняшний день

        Session session = HibernateUtil.getSessionFactory().openSession();

        if (session.createNativeQuery("SELECT * FROM stock WHERE date = " + toDay, Stock.class).getResultList().isEmpty()) {  // убеждаемся в отсутствии данных за текущую дату

            getElements("https://smart-lab.ru/q/usa/", 15);
            getElements("https://smart-lab.ru/q/shares/", 19);

            try {
                for (int i = 0; i < name.size(); i++) {
                    session = HibernateUtil.getSessionFactory().openSession();  // открытие сессии. главная строка для гибернейта
                    session.beginTransaction();  // создание поинта для возврата
                    Stock stock = Main.context.getBean("stock", Stock.class);
                    stock.setName(name.get(i));  // запись названия в БД поле Name
                    stock.setCost(cost.get(i));  // запись стоимости в БД поле Cost
                    stock.setDate(toDay);  // запись даты в БД поле Date
                    session.save(stock);  // делает сохранение без отправки
                    session.getTransaction().commit(); //заливает сохраненные данные до закрытия через флюш
                }
            } catch (Throwable e) {
                session.getTransaction().rollback();  // откатывает к началу транзакции
                session.close();  // всё равно завершает все отложенные транзакции
                ChatBot.sendMassage("Ошибка при добавлении данных в БД");
            }
        } else {
            ChatBot.sendMassage("Повторная дата");
            session.close();
        }
    }

}