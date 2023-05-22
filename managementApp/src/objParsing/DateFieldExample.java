package objParsing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DateFieldExample extends JFrame {
    private final JTextField dateTextField;

    public DateFieldExample() {
        // Set up the JFrame
        super("Date Parser");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);

        // Create the JPanel
        JPanel panel = new JPanel();

        // Create the JLabel and add it to the JPanel
        JLabel label = new JLabel("Enter a date (yyyy-MM-dd):");
        panel.add(label);

        // Create the JTextField and add it to the JPanel
        dateTextField = new JTextField(10);
        panel.add(dateTextField);

        // Create the JButton and add an ActionListener to it
        JButton button = new JButton("Parse Date");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the date from the text field
                String dateString = dateTextField.getText();

                // Parse the date using SimpleDateFormat
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date date = format.parse(dateString);

                    // Print the date to the console
                    System.out.println("Parsed date: " + date.toString());
                } catch (ParseException ex) {
                    // If parsing fails, print an error message to the console
                    System.out.println("Invalid date format");
                }
            }
        });
        panel.add(button);

        // Add the JPanel to the JFrame
        add(panel);

        // Make the JFrame visible
        setVisible(true);
    }

    public static void main(String[] args) {
        new DateFieldExample();
    }
}
