package forMain;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.ApiContextInitializer;


@Configuration
@ComponentScan("forMain")
@EnableScheduling
public class Main {

    public static AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Main.class);

    public static void main(String[] args) {
        ApiContextInitializer.init();

    }

}