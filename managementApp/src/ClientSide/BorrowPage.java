package ClientSide;

import objParsing.GenericTableModel;
import objParsing.TableResponseContainer;
import objParsing.serverResponse;
import objParsing.clientMssg;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
public class BorrowPage extends JFrame {
    private JPanel mainPanelBP;
    private JTable booksTable;
    private JTextField titleVal;
    private JTextField authorVal;
    private JLabel titlevalueLabel;
    private JButton viewButton;
    private JButton homePageButton;
    private JButton borrowButton;
    private JPanel bBtnPanel;
    private JLabel borrowMainStatusLb;
    private JLabel borrowDateLabelInstr;
    private JLabel welcomeLabelBP;
    private JFormattedTextField returnDate;
    private JLabel dateFormatLabel;
    private JPanel viewBooksPanel;
    private JPanel tablePanel;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    Student student;

    /**
     * Initial page that runs when the user signs in and clicks on the borrow button from the homePage
     *
     * @param student this is the user that has signed in to the system
     */
    public BorrowPage(Student student) {
        super();
        reconnectToServer();
        this.student = student;
        this.setTitle("Welcome to the Library Application" + student.getFirstName());
//      Button action listeners
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = null;
                String bookTitle = titleVal.getText().trim();
                String bookAuthor = authorVal.getText().trim();
                if (sortUserInputSize(authorVal.getText().trim(), titleVal.getText().trim())) {
//                    if the author text field is empty then its only title the user typed
                    if (authorVal.getText().isBlank()) {
                        userInput = String.format("%s$%s", "Title only", bookTitle);
                    }
//                    if the title text field is empty then its only title the user typed
                    else if (titleVal.getText().isBlank()) {
                        userInput = String.format("%s$%s", "Author only", bookAuthor);
                    }
//                    if the author text field is not empty and the title texfield is also not empty
                    else if (!(authorVal.getText().isBlank()) && !(titleVal.getText().isBlank())) {
//                        sending author and title
                        userInput = String.format("%s$%s$%s", "Both", bookAuthor, bookTitle);
                    }
//                    pass the concatenated string to the server processing method
                    ViewBooksActions(userInput);

                } else {
                    borrowMainStatusLb.setText("The text field contains some error please check and try again ");
                }
            }
        });
        borrowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Object> userInput = printSelectedRow((GenericTableModel) booksTable.getModel(), booksTable);
                boolean checkbit = false;
//                check if the user selected any entry from the table to make sure we have the data to be sent
                if (userInput.size() > 1) {
//                    extract the  selection of the user from the table (ISBN, Title, Author,Quantity and StudentID)
                    Random rand = new Random();
                    // Generate a random number between 0 and 9999
                    int randomNumber = rand.nextInt(10000);
                    userInput.add(4, randomNumber);
//                 get the value of the return date text-field
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    Date date;
                    try {
                        date = simpleDateFormat.parse(returnDate.getText());
                        checkbit = true;
                    } catch (ParseException ex) {
                        borrowMainStatusLb.setText(ex.getMessage() + " please use this format yyyy/MM/dd");
                        throw new RuntimeException(ex);
                    }
//                 make sure the return date is not empty
                    if (checkbit) {
//                     send the user input to the server for processing using the method
                        borrowBook(userInput, date);
                    }
//                 prompt the user to enter a return date on the status label
                    else {
                        borrowMainStatusLb.setText("Please enter a valid return date using the format yyyy/mm/dd and try again");
                        borrowDateLabelInstr.setText("");
                    }
                }
//                 prompt the user to select a book on the status label
                else {
                    borrowMainStatusLb.setText("Please select a book from the table to continue");
                    borrowDateLabelInstr.setText("");
                }
            }
        });
        homePageButton.addActionListener(new ActionListener() {
            /**
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                showHomepage(student);
            }
        });
    }

    public void initBorrowPage() {
        this.setContentPane(mainPanelBP);
        bBtnPanel.setVisible(false);
        welcomeLabelBP.setText("Welcome to the borrow page " + this.getTitle() + " You can type in the author and the title title of the book you wish to borrow ");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowsClosing(WindowEvent e) {
                super.windowClosing(e);
                closeConnection();
                System.exit(0);
            }
        });
        this.pack();
        this.setVisible(true);
    }

    private void closeConnection() {
        if (socket != null) {
            System.out.println("Status: Closing connection");
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(homePage.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                socket = null;
            }
        }
    }

    /**this method checks the text fields for title and author are not empty */
//    public boolean sortUserInput(String author, String title){
//        return author.matches("^[A-Za-z]+");
//    }

    /**
     * This method check to see that at least one of the text-fields on the GUI  contains only valid alphabets letters.
     **/
    public static boolean sortUserInputSize(String author, String title) {
        boolean checkbit = false;
//        Steps
//        1. if the title text-field contains text and not numbers becasue books titles could be numbers like 1984
        if (!author.isBlank()) {
//            title.trim();
            checkbit = true;
            return checkbit;
        } else if (!title.isBlank()) {
            checkbit = true;
            return checkbit;
        }
        return checkbit;
    }

    /**
     * Checks for the available books that match the users input of either title or author or even both
     *
     * @param userInput this is used to pass the user input from the text field provided
     **/
    public synchronized void ViewBooksActions(String userInput) {
        if (objectInputStream != null && objectOutputStream != null) {
//            Steps
//            1. send the data to the server using object streams
            try {
                objectOutputStream.writeObject(new clientMssg(clientMssg.clientCommands.CHECKFORBOOKUSERINPUT, userInput));
                // TODO: 29/03/2023 check and adjust accordingly to the corresponding section in the thread handler class separating the sending inputs
            } catch (IOException e) {
//                    displaying the input/output exceptions in a label
                borrowMainStatusLb.setText("IOException " + e);
            }
//                2.Receiving the response from the client to update the GUI Table on the borrow page
            TableResponseContainer responseContainer = null;
            try {
//                    reading the response from the server on from the request sent if any
                responseContainer = (TableResponseContainer) objectInputStream.readObject();
                String sqlResultStat = responseContainer.getStatus();
                if (Objects.isNull(sqlResultStat) || sqlResultStat.isBlank()) {
//                        if there is no record from the database for that book requested by the user
                    borrowMainStatusLb.setText("No Results for" + authorVal.getText() + " and book " + titleVal.getText());
                    authorVal.setText("");
                    titleVal.setText("");
//                        booksTable.setModel(new GenericTableModel(responseContainer.columns, responseContainer.data));
//                        if(booksTable.getRowSelectionAllowed())
                } else {
                    borrowMainStatusLb.setText(responseContainer.getStatus());
//                        set the table model to display the results from the database
                    booksTable.setModel(new GenericTableModel(responseContainer.columns, responseContainer.data));
                    borrowDateLabelInstr.setText("Please select the book you want to borrow then click the borrow button");
                    bBtnPanel.setVisible(true);
                    bBtnPanel.updateUI();
                    dateFormatLabel.setText("Enter a date (yyyy/MM/dd):");
                }
//                    booksTable.setModel(new GenericTableModel(responseContainer.columns, responseContainer.data));
            } catch (IOException e) {
                borrowMainStatusLb.setText("IOException " + e);
            } catch (ClassNotFoundException e) {
                borrowMainStatusLb.setText("Class not found exception " + e);
            }
        } else {
            borrowMainStatusLb.setText("Connection to server not established !!!");
        }
    }

    /**
     * This Method is responsible for handling the users request for borrowing a book selecting the book and entering the return date
     *
     * @param userInput this is used to pass the users input (i.e the input required to for borrowing the book)to the server
     * @param date      the  data for borrowing
     **/
    public void borrowBook(List<Object> userInput, Date date) {
//        Steps
//        1a. sort out the user input (i.e. the books information and the date of  return)
        if (objectInputStream != null && objectOutputStream != null) {
            // Steps
            // 1. get the user return date for the selected book
            try {
                objectOutputStream.writeObject(new clientMssg(clientMssg.clientCommands.BORROWBOOK, userInput, date));

            } catch (IOException e) {
                borrowMainStatusLb.setText("IOException " + e);
            }
            // 2 recieve respond from serverside to display
            serverResponse response = null;
            try {
                response = (serverResponse) objectInputStream.readObject();
                int sqlResultStat = response.getServerResponseFlag();
                if (sqlResultStat <= 0) {
//                        if there is no record from the database for that book requested by the user
                    borrowMainStatusLb.setText(response.getMssgToDisplay());
                    returnDate.setText("");
                    authorVal.setText("");
                    titleVal.setText("");
                    booksTable.clearSelection();

//                        booksTable.setModel(new GenericTableModel(responseContainer.columns, responseContainer.data));
//                        if(booksTable.getRowSelectionAllowed())
                } else {
                    borrowMainStatusLb.setText(response.getMssgToDisplay());
                    borrowDateLabelInstr.setText("");
                    returnDate.setText("");
                    authorVal.setText("");
                    titleVal.setText("");
                    booksTable.setVisible(false);
//                    replace with making the table invisible

                }

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * This method is responsible for arranging the input from the table (i.e. the books selected by the user)
     *
     * @param model table model used to extract the row selected
     **/
    public List<Object> printSelectedRow(GenericTableModel model, JTable Table) {
        List<Object> mixedValues = new ArrayList<>();
        mixedValues.add(student.getStudentID());
        int selectedRow = Table.getSelectedRow();
        if (selectedRow != -1) {
            for (int column = 0; column < model.getColumnCount(); column++) {
                Object value = model.getValueAt(selectedRow, column);
                mixedValues.add(value);
            }
            System.out.println(mixedValues);
        } else {
            return mixedValues;
        }
        return mixedValues;
    }

    public void showHomepage(Student signedStudent) {
        homePage homePage = new homePage(signedStudent);
        this.setVisible(false);
        homePage.setVisible(true);
    }

    private void endConnection() {
        if (socket != null) {
            System.out.println("Status: Closing connection");
            try {
                socket.close();

            } catch (IOException ex) {
                Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                socket = null;
            }
        }
    }

    public void reconnectToServer() {
        endConnection();
        borrowMainStatusLb.setText("Status: Attempting connection to server");
        try {
            socket = new Socket("127.0.0.1", 2000);

            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            borrowMainStatusLb.setText("Status: Connected to server");
        } catch (IOException ex) {
            Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
            borrowMainStatusLb.setText(ex.toString()); // connection failed
        }
    }

    /**
     * This gets the data the user entered on the GUI and process the information to send to the server.
     *
     * @param borrowDate the entered user data in this format yyyy/mm/dd
     **/

    public Date getDate(String borrowDate) {
//        variable is a Date expected to return a Date
        Date convertdate = new Date();
//        create a date format and set the pattern of the date
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
//        convert the string from the GUI to a date in a try/catch block
        try {
            convertdate = format.parse(borrowDate);
            // Print the date to the console
            System.out.println("Parsed date: " + convertdate.toString());
            return convertdate;
        } catch (ParseException ex) {
            // If parsing fails, print an error message to the console
            return convertdate;
        }
    }

}

