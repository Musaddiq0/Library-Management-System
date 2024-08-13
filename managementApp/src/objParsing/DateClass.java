package objParsing;
import java.time.LocalDate;

public class DateClass {
    private String name;
    private LocalDate eventDate;

    public DateClass(String name, LocalDate eventDate) {
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