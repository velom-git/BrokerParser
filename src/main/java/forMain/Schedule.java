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
    private ForBD forBD;

    @Scheduled(cron = "0 50 21 * * MON-FRI", zone = "Europe/Moscow")
    public void scheduledMethod() {
        try {
            new TelegramBotsApi().registerBot(new ChatBot());
            parser.start();
            forBD.getCompany();

        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }


    }
}
