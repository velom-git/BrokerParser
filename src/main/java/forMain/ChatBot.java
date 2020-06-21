package forMain;


import org.apache.http.client.fluent.Request;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;


public class ChatBot extends TelegramLongPollingBot {


    @Override
    public String getBotUsername() {
        return "ИМЯВАШЕГОБОТА";
    }

    @Override
    public String getBotToken() {
        return "ТОКЕНВАШЕГОБОТА";
    }

    public static void sendMassage(String text) {
        String textCLean = text.replaceAll("[\\pP\\s&&[^%0A]]","%20");  //выкидываем лишние символы из названий кампаний потому, что нам это передавать через url
        try {
            Request.Get("https://api.telegram.org/ТОКЕНВАШЕГОБОТА/sendMessage?chat_id=НОМЕРЧАТАСВАШИМБОТОМ&text="+textCLean).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {}
}
