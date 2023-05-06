package ClientSide;

import objParsing.GenericTableModel;
import objParsing.TableResponseContainer;
import objParsing.serverResponse;
import objParsing.clientMssg;

import javax.swing.*;
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
public class BorrowPage extends JFrame{
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
    private JLabel borrowLabelInstr;
    private JLabel welcomeLabelBP;
    private JFormattedTextField returnDate;
    private JLabel dateFormatLabel;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
     String stdID;
     String stdname;
    /**Initial page that runs when the user signs in and clicks on the borrow button from the homePage
     * @param stuName this is the user that has signed in to the system
     * @param studentID  this is the ID for the logged-in user*/
    public BorrowPage (String stuName, String studentID){
        super();
        reconnectToServer();
        this.stdID = studentID;
        this.stdname = stuName;

        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String [] userinput = new String[2];
                String userToServ = null;
                if(checkIfNotBlank(authorVal.getText(), titleVal.getText())){
                    userinput[0] = authorVal.getText();
                    userinput[1] = titleVal.getText();
//                    if the first value of the array is empty then its only title the user typed
                    if(userinput[0].isBlank()){
                        userToServ =  userinput[1];
                        userToServ = String.format("%s:%s", "Title only",userinput[1]);                    }
                    else if (userinput[1].isBlank()){
                        userToServ = userinput[0] ;
                        userToServ = String.format("%s:%s", "Author only",userinput[0]);
                    }
                    else if(!(userinput[0].isBlank()) && !(userinput[1].isBlank())){
                        userToServ = String.format("%s:%s:%s","Both",userinput[0],userinput[1]);
                    }

                    checkForBooks(userToServ);
                }else{
                    borrowMainStatusLb.setText("The text field contains some error please check and try again ");
                }
            }
        });
        borrowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List <Object> userInput = printSelectedRow((GenericTableModel) booksTable.getModel());
//                check if the user selected any entry from the table to make sure we have the data to be sent
                if(userInput.size()>2){
//                    extract the  selection of the user from the table (ISBN, Title, Author,Quantity and StudentID)
                    Random rand = new Random();

                    // Generate a random number between 0 and 99
                    int randomNumber = rand.nextInt(10000);
                    userInput.add(4,randomNumber);
//                 get the value of the return date text-field
                    String userDate = returnDate.getText();
                    Date date = new Date();
//                    setting the user date to the format
                    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                    try {
                        date = format.parse(userDate);
                        // Print the date to the console
                         System.out.println("Parsed date: " + date.toString());
                    } catch (ParseException ex) {
                        // If parsing fails, print an error message to the console
                        System.out.println("Invalid date format");
                        userDate = "invalid";
                    }
//                 make sure the return date is not empty
                 if(!(Objects.equals(userDate, "invalid"))){
//                     send the user input to the server for processing using the method
                     borrowBook(userInput, date);
                 }
//                 prompt the user to enter a return date on the status label
                 else{
                     borrowMainStatusLb.setText("Please enter a valid return date using the format yyyy/mm/dd and try again");
                     borrowLabelInstr.setText("");
                 }
                }
//                 prompt the user to select a book on the status label
                else{
                    borrowMainStatusLb.setText("Please select a book from the table to continue");
                    borrowLabelInstr.setText("");
                }
            }
        });
    }

    public void initBorrowPage(){
        this.setContentPane(mainPanelBP);
        bBtnPanel.setVisible(false);
        welcomeLabelBP.setText("Welcome to the borrow page " +this.getTitle()+ " You can type in the author and the title title of the book you wish to borrow ");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addWindowListener( new WindowAdapter(){
            public void windowsClosing(WindowEvent e){
                super.windowClosing(e);
                closeConnection();
                System.exit(0);
            }
        });
        this.pack();
        this.setVisible(true);
    }

    private void closeConnection() {
        if(socket != null){
            System.out.println("Status: Closing connection");
            try{
                socket.close();
            }catch (IOException ex){
                Logger.getLogger(homePage.class.getName()).log(Level.SEVERE,null,ex);
            }finally {
                socket = null;
            }
        }
    }

    /**this method checks the text fields for title and author are not empty */
    public boolean sortUserInput(String author, String title){
        if(!(author.isBlank()) && author.matches("^[A-Za-z]+") ){
            return true;
        }
        else return !(title.isBlank()) && (title.matches("^[A-Za-z]+"));
    }

    public static boolean checkIfNotBlank(String author, String title) {
        if (!author.isBlank() || !title.isBlank()) {
            return true;
        } else {
            return false;
        }
    }


    /**
    *  Checks for the available books that match the users input of either title or author or even both
     *  @param userInput this is used to pass the user input from the text field provided
     **/
    public synchronized void checkForBooks(String userInput) {
        if (objectInputStream != null && objectOutputStream!=null){
            try {
                    objectOutputStream.writeObject(new clientMssg(clientMssg.clientCommands.VIEWBOOKSUSINGAT, userInput));
                    // TODO: 29/03/2023 check and adjust accordingly to the corresponding section in the thread handler class separating the sending inputs
                } catch (IOException e) {
//                    displaying the input/output exceptions in a label
                    borrowMainStatusLb.setText("IOException " + e);
                }
//                Receiving the response from the client to update the GUI Table on the borrow page
                TableResponseContainer responseContainer =null;
                try {
//                    reading the response from the server on from the request sent if any
                    responseContainer = (TableResponseContainer) objectInputStream.readObject();
                    String sqlResultStat = responseContainer.getStatus();
                    if(Objects.isNull(sqlResultStat)||sqlResultStat.isBlank()){
//                        if there is no record from the database for that book requested by the user
                        borrowMainStatusLb.setText("No Results for" +authorVal.getText() + " and book " +titleVal.getText());
                        authorVal.setText("");
                        titleVal.setText("");
//                        booksTable.setModel(new GenericTableModel(responseContainer.columns, responseContainer.data));
//                        if(booksTable.getRowSelectionAllowed())
                    }else{
                        borrowMainStatusLb.setText(responseContainer.getStatus());
//                        set the table model to display the results from the database
                        booksTable.setModel(new GenericTableModel(responseContainer.columns, responseContainer.data));
                        borrowLabelInstr.setText("Please select the book you want to borrow then click the borrow button");
                        bBtnPanel.setVisible(true);
                        bBtnPanel.updateUI();
                        dateFormatLabel.setText("Enter a date (yyyy/MM/dd):");
                    }
                    booksTable.setModel(new GenericTableModel(responseContainer.columns, responseContainer.data));
                } catch (IOException e) {
                    borrowMainStatusLb.setText("IOException " + e);
                } catch (ClassNotFoundException e) {
                    borrowMainStatusLb.setText("Class not found exception " +e);
                }
            } else{
            borrowMainStatusLb.setText("Connection to server not established !!!");
        }
    }

    public void borrowBook(List<Object> userInput, Date date){
//        Steps
//        1a. sort out the user input (i.e. the books information and the date of  return)
        if(objectInputStream != null && objectOutputStream != null){
            // Steps
            // 1. get the user return date for the selected book
            try{
                objectOutputStream.writeObject(new clientMssg(clientMssg.clientCommands.BORROWBOOK, userInput, date));

            }catch (IOException e){
                borrowMainStatusLb.setText("IOException " + e);
            }
            // 2 recieve respond from serverside to display
                serverResponse response = null;
            try{
                response = (serverResponse) objectInputStream.readObject();
                int sqlResultStat = response.getBorrowFlag();
                if(sqlResultStat<=0){
//                        if there is no record from the database for that book requested by the user
                    borrowMainStatusLb.setText(response.getMssgToDisplay());
                    returnDate.setText("");
                    authorVal.setText("");
                    titleVal.setText("");
                    booksTable.clearSelection();

//                        booksTable.setModel(new GenericTableModel(responseContainer.columns, responseContainer.data));
//                        if(booksTable.getRowSelectionAllowed())
                }else{
                    borrowMainStatusLb.setText(response.getMssgToDisplay());
                    borrowLabelInstr.setText("");
                    returnDate.setText("");
                    authorVal.setText("");
                    titleVal.setText("");
                    booksTable.clearSelection();

                }

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public List<Object> printSelectedRow(GenericTableModel model) {
        List<Object> mixedValues = new ArrayList<>();
        mixedValues.add(this.stdID);
        mixedValues.add(this.stdname);
        int selectedRow = booksTable.getSelectedRow();
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


    private void endConnection() {
        if(socket != null){
            System.out.println("Status: Closing connection");
            try{
                socket.close();

            }catch (IOException ex){
                Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE,null,ex);
            }finally {
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

    public Date getDate() {
        // Get the user's input from the date field and parse it as a Date object
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            return format.parse(returnDate.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format!");
            return null;
        }
    }
}
