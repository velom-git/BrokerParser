package forMain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Component
@EnableScheduling
public class Schedule {

    @Autowired
    private Parser parser;
    @Autowired
    private WorkingWithBD workingWithBD;

    @Scheduled(cron = "0 30 15 * * MON-FRI", zone = "Europe/Moscow")
    public void scheduledMethod() {
        try {
            new TelegramBotsApi().registerBot(new ChatBot());
            parser.addEntities(); // отвечает за парсинг страницы и добавления информации в БД
            workingWithBD.getCompaniesNames(); // получаем 2 списка с названиями кампаний, которые были в торгах сегодня и 7 дней назад
            workingWithBD.mathAndSend();  // высчитываем дельту и отправляем список нужных кампаний в телегу

        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

}
