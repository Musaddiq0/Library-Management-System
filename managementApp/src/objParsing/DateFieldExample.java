package objParsing;
import java.time.LocalDate;
import java.util.Date;

public class DateFieldExample {
    private String name;
    private LocalDate eventDate;

    public DateFieldExample(String name, LocalDate eventDate) {
        this.name = name;
        this.eventDate = eventDate;
    }

    public String getName() {
        return name;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }
}