package ClientSide;
import objParsing.GenericTableModel;
import objParsing.TableResponseContainer;
import objParsing.clientMssg;
import objParsing.serverResponse;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class homePage  extends JFrame{

    protected JPanel homepageMainPanel;
    JLabel hpWelcomeLB;
    private JButton userInformationButton;
    private JButton borrowPageButton;
    private JLabel welcomLabel;
    JLabel statusLabelHP;
    private JButton loginPageButton;
    private JPanel homepageButtonsPanel;
    private JButton returnBookMenuButton;
    private JTextField borrowIDtxt;
    private JTextField bookTitleVal;
    private JButton homePageButton;
    private JButton returnButton;
    private JPanel ReturnPanel;
    private JTable borrowedBooksTable;
    private JFormattedTextField returnDateVal;
    private JPanel userInfoPanel;
    //    protected JPanel booksPanel;
//    protected JPanel categoryPanel;
//    private JPanel BorrowingPanel;
//    String statusLabelHPText = statusLabelHP.getText();
//    UserInterface userInterface = new UserInterface("Library Management App");
    Student student;
    ObjectOutputStream objectOutputStream ;
    ObjectInputStream objectInputStream;
    public homePage(Student curStudent){
        super();
        this.student = curStudent;
        showHomepage();
        userInformationButton.addActionListener(e -> showUsrInforPage(student));
        loginPageButton.addActionListener(e -> loginPageSetUp());
        borrowPageButton.addActionListener(e -> showBorrowPage());
        returnBookMenuButton.addActionListener(new ActionListener() {
            /**
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                ReturnPanel.setVisible(true);
                homepageButtonsPanel.setVisible(false);
                showBorrowedBooks(student);
            }
        });
        returnButton.addActionListener(new ActionListener() {
            /**
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!bookTitleVal.getText().isEmpty()){
                    returnBook(curStudent,bookTitleVal.getText());
                }
                else {
                    statusLabelHP.setText("Please check and make your you have the filled in the right details");
                }

            }
        });
        homePageButton.addActionListener(new ActionListener() {
            /**
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                ReturnPanel.setVisible(false);
                homepageButtonsPanel.setVisible(true);


            }
        });
    }

    private void showBorrowedBooks(Student student) {
        if(objectInputStream != null && objectOutputStream!=null){
//            sending the user details to get details of books borrowed if any
            try{
                objectOutputStream.writeObject(new clientMssg(clientMssg.clientCommands.BORROWEDBOOKSMENUINFO, student));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            reading the server reply to display on the table.
            TableResponseContainer responseContainer = null;

            try{
                responseContainer = (TableResponseContainer)objectInputStream.readObject();
                if(responseContainer.getBorrowedCode() != 1){
                    statusLabelHP.setText(responseContainer.getStatus());
//                        set the table model to display the results from the database
                    borrowedBooksTable.setModel(new GenericTableModel(responseContainer.columns, responseContainer.data));
                    ReturnPanel.updateUI();
                }
                else{
                    statusLabelHP.setText(responseContainer.getStatus());
                }
            }catch (IOException e ){
                statusLabelHP.setText("IOE exception occurred" + e);
            } catch (ClassNotFoundException e) {
                statusLabelHP.setText("ClassNotFoundException occurred" + e);
            }

        }
    }

    private synchronized void returnBook(Student student, String bookTitle) {
        if(objectInputStream != null && objectOutputStream!= null){
//Steps
//            1.  send data to the server to update the DB (Student object and the book title)
            try{
                objectOutputStream.writeObject(new clientMssg(clientMssg.clientCommands.RETURNBOOK, student, bookTitle));
            } catch (IOException e) {
                throw new RuntimeException("IOException " +e);
            }
//            2. read the reply from the server
            serverResponse response =null;
            try {
                response= (serverResponse) objectInputStream.readObject();

            }catch (ClassNotFoundException e) {
                statusLabelHP.setText("Class not found exception " +e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private Socket socket;
    private void showHomepage() {
        reconnectToServer();
        this.setContentPane(homepageMainPanel);
        ReturnPanel.setVisible(false);
        welcomLabel.setText("Welcome to the Library application " +student.getFirstName());
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setSize(800,800);
        this.setVisible(true);
        this.pack();
        this.addWindowListener( new WindowAdapter(){
        });
    }
    public void showBorrowPage(){
        this.setVisible(false);
        BorrowPage borrowPage =new BorrowPage(student);
        borrowPage.initBorrowPage();

    }

/**This shows the user information page and terminates the present UI.
 * @param student current logged in student data **/
    public void showUsrInforPage(Student student){
        UserInfor userInfor = new UserInfor(student);
        userInfor.showUsrInfo();
        userInfor.setVisible(true);
        this.setVisible(false);
    }

    private void loginPageSetUp(){
        UserInterface userInterface = new UserInterface(homePage.super.getTitle());
        userInterface.setVisible(true);
        this.setVisible(false);
    }

    /**This method is used to gather the user input and make sure that the user has entered something in the text-field provided
     * i.e. it returns an list containing the data entered by the user**/
//    public List<Object> validUserInput (){
////        Steps
////        1. collect the text-field data entered by the user and create list to return colected values
//        List<Object> UserInput = new ArrayList<>();
////        1a.borrowID and bookTitle
//        String borrowID = borrowIDtxt.getText();
//        String booktitle = bookTitleVal.getText();
////        1b. returndate
//        Date date;
//        try {
//            date = rawdate.parse(returnDateVal.getText());
//        } catch (ParseException ex) {
//            statusLabelHP.setText(ex.getMessage()+" please use this format yyyy/MM/dd");
//            throw new RuntimeException(ex);
//        }
//        if (!borrowID.isBlank() && !booktitle.isBlank() && !date.toString().isBlank()){
//            UserInput.add(borrowID);
//            UserInput.add(booktitle);
//            UserInput.add(date);
//            return UserInput;
//        }
//        else {
//            return UserInput;
//        }
//    }



    private void endConnection() {
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
    public void reconnectToServer() {
        endConnection();
        statusLabelHP.setText("Status: Attempting connection to server");
        try {
            socket = new Socket("127.0.0.1", 2000);

            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            statusLabelHP.setText("Status: Connected to server");
        } catch (IOException ex) {
            Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
            statusLabelHP.setText(ex.toString()); // connection failed
        }
    }
    public static void main (String[] args){
     homePage page = new homePage(new Student(0,"",""));

    }
}
