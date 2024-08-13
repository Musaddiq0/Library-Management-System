package ClientSide;

import objParsing.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private JLabel dateFormatMssg;
    private JPanel viewBooksPanel;
    private JPanel tablePanel;
    private JButton viewAllButton;
    private JButton viewAllBooksButton;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    Student student;
    Random rand = new Random();
/*

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
                authorVal.addKeyListener(new KeyAdapter() {
                    /**
                     * @param e the event to be processed
                     */
                    @Override
                    public void keyTyped(KeyEvent e) {
                        char n = e.getKeyChar();
                        if(!Character.isLetter(n)){
                            e.consume();
                            JOptionPane.showMessageDialog(mainPanelBP,"Please enter only alphabets");
                        }
                    }
                });
                titleVal.addKeyListener(new KeyAdapter() {
                    /**
                     * @param e the event to be processed
                     */
                    @Override
                    public void keyTyped(KeyEvent e) {
                        char n = e.getKeyChar();
                        if(!Character.isLetter(n) && !Character.isDigit(n)){
                            e.consume();
                            JOptionPane.showMessageDialog(mainPanelBP,"Please enter only alphabets or numbers");
                        }
                    }
                });
                String userInput = null;
                String bookTitle = titleVal.getText().trim();
                String bookAuthor = authorVal.getText().trim();
//                    if the author text field is empty then its only title the user typed
                    if (bookAuthor.isBlank()) {
                        userInput = String.format("%s$%s", "Title only", bookTitle);
                        //pass the concatenated string to the server processing method
                        ViewBooksActions(userInput);
                    }
//                    if the title text field is empty then its only title the user typed
                    else if (bookTitle.isBlank()) {
                        userInput = String.format("%s$%s", "Author only", bookAuthor);
                        //pass the concatenated string to the server processing method
                        ViewBooksActions(userInput);
                    }
//                    if the author text field is not empty and the title texfield is also not empty
                    else if (!bookAuthor.isEmpty() && !bookTitle.isEmpty()) {
//                        sending author and title
                        userInput = String.format("%s$%s$%s", "Both", bookAuthor, bookTitle);
//                        pass the concatenated string to the server processing method
                        ViewBooksActions(userInput);
                    }
                    else if(bookAuthor.isEmpty() && bookTitle.isEmpty()) {
                        borrowMainStatusLb.setText("The text field contains some error please check and try again ");
                    }
            }
        });
        borrowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                boolean checkbit = false;
                List <Object> userBorrowData = new ArrayList<>();
                int random = rand.nextInt(1000);
                userBorrowData.add(random);
                if(isBorrowInputValid((GenericTableModel)booksTable.getModel(), booksTable, userBorrowData)){
                    System.out.println("hello");
//                    get the value of the return date text-field using the DateClass
                    DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate userDate = null;
                    try {
                        userDate = LocalDate.parse(returnDate.getText().trim(), simpleDateFormat);
                        LocalDate localDate = LocalDate.now();
                        if(userDate.isAfter(localDate)) {
                            DateFieldExample dateFieldExample = new DateFieldExample("Borrow Date",userDate);
                            userBorrowData.add(userDate);
//                            student.setBorrowDate(userDate);
//                            student.setBorrowBooksRequest(userBorrowData);
                            borrowBook(userBorrowData,dateFieldExample);
                        }

                    } catch (DateTimeException ex) {
                        borrowMainStatusLb.setText(ex.getMessage() + " please use this format dd/MM/yyyy");
                        throw new RuntimeException(ex);
                    }
                }
//                check if the user selected any entry from the table to make sure we have the data to be sent
//                if (userInput.size() > 1) {
////                    extract the  selection of the user from the table (ISBN, Title, Author,Quantity and StudentID)
//                    // Generate a random number between 0 and 9999 for the borrow ID
//                    int randomNumber = rand.nextInt(10000);
//                    userInput.add(4, randomNumber);
////                 get the value of the return date text-field using the DateClass
//                    DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern("dd/MM/yy");
//                    LocalDate userDate = null;
//                    try {
//                        userDate = LocalDate.parse(returnDate.getText(), simpleDateFormat);
//                        LocalDate localDate = LocalDate.now();
//                        if(userDate.isAfter(localDate)) {
//                            checkbit = true;
//                        }
//
//                    } catch (DateTimeException ex) {
//                        borrowMainStatusLb.setText(ex.getMessage() + " please use this format dd/MM/yy and enter a valid date");
//                        throw new RuntimeException(ex);
//                    }
////                 make sure the return date is not empty
//                    if (checkbit) {
////                     send the user input to the server for processing using the method
//                        borrowBook(userInput, userDate);
//                    }
////                 prompt the user to enter a return date on the status label
//                    else {
//                        borrowMainStatusLb.setText("Please enter a valid return date using the format yyyy/mm/dd and try again");
//                        borrowDateLabelInstr.setText("");
//                    }
//                }
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

        viewAllButton.addActionListener(new ActionListener() {
            /**
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
//                steps
//                1.no input to expect from user
//                2. call viewallbooks methods
                showAllBooks(student);

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
//    public static boolean sortUserInputSize(String author, String title) {
////        Steps
////        1. if the title text-field contains text and not numbers because books titles could be numbers like 1984
//        if (!author.isBlank()) {
////            title.trim();
//            return true;
//        } else return !title.isBlank();
//    }

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
                    dateFormatMssg.setText("Enter a date (dd/MM/yyyy):");
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
     * @param userInput this is used to pass the  signed-in users input (i.e the input required to for borrowing the book)to the server
     **/
    public void borrowBook(List<Object> userInput, DateFieldExample borrowdate) {
//        Steps
//        1a. sort out the user input (i.e. the books information and the date of  return)
        if (objectInputStream != null && objectOutputStream != null) {
            // Steps
            // 1. get the user return date for the selected book
            try {
                clientMssg clientMssg = new clientMssg(objParsing.clientMssg.clientCommands.BORROWBOOK);
                clientMssg.setBooksSelected(userInput);
                clientMssg.setStudParcels(student);
                objectOutputStream.writeObject(clientMssg);
            } catch (IOException e) {
                borrowMainStatusLb.setText("IOException " + e);
            }
            // 2 receive respond from serverside to display
//            get back a server response object
            serverResponse response ;
//            Update the table
//            update the labels
            try {
                response = (serverResponse) objectInputStream.readObject();

//            update the text fields
                returnDate.setText("");
                authorVal.setText("");
                titleVal.setText("");
                booksTable.clearSelection();
                int sqlResultStat = response.getServerResponseFlag();
                borrowMainStatusLb.setText(response.getMssgToDisplay());
                if(sqlResultStat <= 0) {
//                        something wrong with the DB
                    returnDate.setText("");
                    authorVal.setText("");
                    titleVal.setText("");
                    booksTable.clearSelection();
                    borrowMainStatusLb.setText(response.getMssgToDisplay());
                }
                else {
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
    public synchronized void showAllBooks(Student student){
        if(objectInputStream != null && objectOutputStream != null){
//            steps
//            1. send the client command to the backend
            try {
                objectOutputStream.writeObject(new clientMssg(clientMssg.clientCommands.VIEWALLBOOKS,student));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            listen for server response expecting a data for books table
            TableResponseContainer responseContainer;
            try{
                responseContainer = (TableResponseContainer) objectInputStream.readObject();
//                checking if the response contains any data for response
                if(responseContainer.getStatus().contains("no books available")){
                    borrowMainStatusLb.setText("No results error server problem");
                }
                else{
                    borrowMainStatusLb.setText(responseContainer.getStatus());
//                        set the table model to display the results from the database
                    booksTable.setModel(new GenericTableModel(responseContainer.columns, responseContainer.data));
                    borrowDateLabelInstr.setText("Please select the book you want to borrow then click the borrow button");
                    bBtnPanel.setVisible(true);
                    bBtnPanel.updateUI();
                    dateFormatMssg.setText("Enter a date (dd/MM/yyyy):");

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
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

    /**
     * This gets the data the user entered on the GUI and process the information to send to the server to borrow a book .
     *
     * @param tableModel table model for the table used to select the data
     * @param booksTable books table
     * @param userInput array list containing selected book from table
     **/
    public boolean isBorrowInputValid(GenericTableModel tableModel, JTable booksTable, List<Object> userInput){
        boolean checkBit = false;
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow != -1) {
            for (int column = 0; column < tableModel.getColumnCount(); column++) {
                Object value = tableModel.getValueAt(selectedRow, column);
                userInput.add(value);
            }
            System.out.println(userInput);
        }
        else {
            return  checkBit;
        }
        return !checkBit;
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

//    public

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

//    public Date getDate(String borrowDate) {
////        variable is a Date expected to return a Date
//        Date convertdate = new Date();
////        create a date format and set the pattern of the date
//        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
////        convert the string from the GUI to a date in a try/catch block
//        try {
//            convertdate = format.parse(borrowDate);
//            // Print the date to the console
//            System.out.println("Parsed date: " + convertdate.toString());
//            return convertdate;
//        } catch (ParseException ex) {
//            // If parsing fails, print an error message to the console
//            return convertdate;
//        }
//    }

}

