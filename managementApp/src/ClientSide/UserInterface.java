package ClientSide;

import objParsing.actionClass;
import objParsing.clientMssg;
import objParsing.serverResponse;
import org.jetbrains.annotations.NotNull;

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
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserInterface  extends JFrame {
    private JTabbedPane HomePane;
    private JPanel mainAppPanel;
    private JPanel mainPanel;
    private JLabel statusLabel;
    private JTextField studentIdVal;
    private JTextField lnVal;
    private JTextField fnVal;
    private JLabel studentIDLabel;
    private JLabel lnLabel;
    private JLabel fnLabel;
    private JLabel loginInstLabel;
    private JButton loginButton;
    private JButton cancelButton;
    private JTextField stIDL;
    private JPanel loginPanel;
    private JButton registerButton;
    private JPanel registerPanel;
    private JTabbedPane tabbedPane1;
    private JPanel Register;
    private JPanel Login;
    private JPasswordField passWR;
    private JPasswordField passWLogin;
    private char[] passWord;

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Socket socket;

/**
 * this method performs a input validation check on the input passed
 * @param firstName this is the users first name read from the textfield provided
 * @param lastName  this is the users lastname read from the textfield provided*/
    public boolean sortUserInput(String lastName, String firstName){
        //Steps
        //1. Check and confirm the text fields are not empty or contain garbage
        return ((firstName.matches("^[A-Za-z]+")) && !(firstName.isBlank())) && ((lastName.matches("^[A-Za-z]+")) && !(lastName.isBlank()));

        }

        /**
         * This is the class constructor with the initialization method that builds the login page, it also contains the action listeners for the buttons
         * @param Title this is the firstname of the logged-in user*/
        public UserInterface(String Title) {
        super(Title);
        initGUI();
        reconnectToServer();
        cancelButton.addActionListener(new actionClass());
        loginButton.addActionListener(new ActionListener()  {
            @Override
            public void actionPerformed(ActionEvent e) {
//                //Cautionary Steps to handle exceptions
//                try{
//                }catch (NumberFormatException){
//
//                }
//                1a. Collect and validate  the users input
                int studentID = Integer.parseInt(stIDL.getText());
                String password = Arrays.toString(passWLogin.getPassword());
//                String firstname = fnVal.getText().trim();
//                String lastname = lnVal.getText().trim();
                if(validatePassword(password)){
                    Student potentialStudent = new Student(studentID,password);
                    loginUser(potentialStudent);
                }
//                if(sortUserInput(lastname, firstname)){
////                    Create a student instance to store or destroy upon validation
//                    int studentID = Integer.parseInt(studentIdVal.getText().trim());
//                    Student student = new Student(studentID, firstname, lastname);
//                    //1b. check the user input match what is expected strings and int respectively
//                    loginUser(student);
//                }
               else{
                   reconnectToServer();
                   JOptionPane.showMessageDialog(mainPanel, "Please enter a valid login details in the spaces provided!!!");
//                   try {
//                       student.destroy();
//                   } catch (Throwable ex) {
//                       throw new RuntimeException(ex);
//                   }
               }
            }
        });
        registerButton.addActionListener(new ActionListener() {
                /**
                 * @param e the event to be processed
                 */
                @Override
                public void actionPerformed(ActionEvent e) {
                    //get the values from th text-field
                    studentIdVal.addKeyListener(new KeyAdapter() {
                        /**
                         * @param e the event to be processed
                         */
                        @Override
                        public void keyTyped(KeyEvent e) {
                            char n = e.getKeyChar();
                            if(!Character.isDigit(n)){
                                e.consume();
                                JOptionPane.showMessageDialog(registerPanel,"Please enter only alphabets");

                            }
                        }
                    });
                    fnVal.addKeyListener(new KeyAdapter() {
                        /**
                         * @param e the event to be processed
                         */
                        @Override
                        public void keyTyped(KeyEvent e) {
                            char n = e.getKeyChar();
                            if(!Character.isLetter(n)){
                                e.consume();
                                JOptionPane.showMessageDialog(registerPanel,"Please enter only alphabets");

                            }
                        }
                    });
                    lnVal.addKeyListener(new KeyAdapter() {
                        /**
                         * @param e the event to be processed
                         */
                        @Override
                        public void keyTyped(KeyEvent e) {
                            char n = e.getKeyChar();
                            if(!Character.isLetter(n) && !Character.isWhitespace(n)){
                                e.consume();
                                JOptionPane.showMessageDialog(mainPanel,"Please enter only alphabets");
                            }
                        }
                    });
                    passWord = passWR.getPassword();
                    if(validatePassword(Arrays.toString(passWord))){
                        int stdID = Integer.parseInt(studentIdVal.getText());
                        String firstName = fnVal.getText();
                        String lastName = lnVal.getText();
                        Student newStudent = new Student(stdID,firstName,lastName);
                        newStudent.setPassword(Arrays.toString(passWord));
                        createUser(newStudent);


                    }
                    else {
                        JOptionPane.showMessageDialog(mainAppPanel,"Please enter a password containing numbers and letters!!!");
                    }
                }
            });
        }

    /**This method accepts the student class as the argument and sends the student data for verification on the database
     * @param potentialStudent object of the Student class for easy access to  student data*/
    private synchronized void loginUser(Student potentialStudent) {
        if (objectInputStream != null && objectOutputStream != null){
            //Steps
            //1. Prepare and parse the inputs to the outputStream to send to the server (command and studentship)
            try{
                objectOutputStream.writeObject(new clientMssg(clientMssg.clientCommands.LOGIN, potentialStudent));

            }catch (IOException e){
                statusLabel.setText("IOException " + e);
            }
            //3. receive reply from the server using the object input stream
            clientMssg reply;
            Student confirmedStudent;
            try{
                reply = (clientMssg) objectInputStream.readObject();
                confirmedStudent= (Student)objectInputStream.readObject();
                int loginStat = reply.getLoginStatus();
                if (loginStat==0){
                    loginUnsucessful(confirmedStudent);
                    JOptionPane.showMessageDialog(mainPanel, reply.getStatusMssg());

                }
                else{
                    statusLabel.setText(reply.getStatusMssg());
                    loginSuccess(confirmedStudent);
                }

            }catch (IOException e){
                statusLabel.setText("IOException" + e);

            }catch (ClassNotFoundException e){
                statusLabel.setText("Class not found exception" + e);

            }
        }
        else {
            statusLabel.setText("Connection to the server not established");
        }
    }
    private synchronized void createUser(Student newStudent){
        if (objectInputStream != null && objectOutputStream != null){
            //Steps
            //1. Prepare and parse the inputs to the outputStream to send to the server (command and studentship)
            try{
                objectOutputStream.writeObject(new clientMssg(clientMssg.clientCommands.CREATEUSER, newStudent, Arrays.toString(passWord)));

            }catch (IOException e){
                statusLabel.setText("IOException " + e);
            }
            //3. receive reply from the server using the object input stream
            serverResponse reply = new serverResponse();
            try{
                reply = (serverResponse) objectInputStream.readObject();
                int loginStat = reply.getServerResponseFlag();
                if (loginStat<=0){
                    studentIdVal.setText("");
                    fnVal.setText("");
                    lnVal.setText("");
                    passWR.setText("");
                    JOptionPane.showMessageDialog(mainPanel, "Please try again");
                    newStudent = null;
                }
                else{
//                    statusLabel.setText(reply.getStatusMssg());
                    studentIdVal.setText("");
                    fnVal.setText("");
                    lnVal.setText("");
                    passWR.setText("");
                    JOptionPane.showMessageDialog(mainPanel, reply.getMssgToDisplay());

                }

            }catch (IOException e){
                statusLabel.setText("IOException" + e);

            }catch (ClassNotFoundException e){
                statusLabel.setText("Class not found exception" + e);

            }
        }
        else {
            statusLabel.setText("Connection to the server not established");
        }



    }
    /**This method draws the inital login page GUI*/
    private void initGUI() {
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setSize(800,800);
        this.setVisible(true);
        this.pack();
        this.addWindowListener( new WindowAdapter(){
        });
    }

    /**This method is used to display the homepage when the user's log-in details are correct
     * @param curStudent this uses the student class object of the signed in student*/
    private void loginSuccess(Student curStudent){
        homePage page= new homePage(curStudent);
            page.statusLabelHP.setText("Connection to the server established");
        this.setVisible(false);

    }
    private void loginUnsucessful(Student fakeStudent){
        stIDL.setText(null);
        passWLogin.setText(null);
    }

//    protected String loginUser(){
//        return fnVal.getText();
//    }

    /**This method terminates the socket and thread connection used when a thread has ended its task*/
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
    /**This method checks the password contains numbers and letters
     * @param password This is from the user input
     * **/
    private static boolean validatePassword(@NotNull String password){
        boolean hasNumber= false;
        boolean hasAlphabet= false;

        for(char c: password.toCharArray()){
            if (Character.isLetter(c)){
                hasAlphabet =true;
            }
            if(Character.isDigit(c)){
                hasNumber =true;
            }
            if(hasAlphabet&&hasNumber){
                return true;
            }
        }
        return  false;
    }

    /**This method reconnects the socket and makes connection to the server*/
    public void reconnectToServer() {
        endConnection();
        statusLabel.setText("Status: Attempting connection to server");
        try {
            socket = new Socket("127.0.0.1", 2000);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            statusLabel.setText("Status: Connected to server");
        } catch (IOException ex) {
            Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
            statusLabel.setText(ex.toString()); // connection failed
        }
    }


    public static void main(String[] args) {
        UserInterface first = new UserInterface("Library Management App");
    }



    private static boolean isNumeric(String str) {
        return str.matches("\\d+");
    }

    private void createUIComponents() {
        studentIdVal = new JTextField(15);
        AbstractDocument document = (AbstractDocument) studentIdVal.getDocument();
        document.setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                currentText += text;

                if (isNumeric(text) && currentText.length() <= 10) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        fnVal = new JTextField(20);
            AbstractDocument doc = (AbstractDocument) fnVal.getDocument();
            doc.setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    // Filter out spaces when inserting text
                    string = string.replaceAll("\\s", "");
                    super.insertString(fb, offset, string, attr);
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    // Filter out spaces when replacing text
                    text = text.replaceAll("\\s", "");
                    super.replace(fb, offset, length, text, attrs);
                }
            });
            lnVal = new JTextField(20);
            AbstractDocument document1 = (AbstractDocument) lnVal.getDocument();
        document1.setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    // Filter out spaces when inserting text
                    string = string.replaceAll("\\s", "");
                    super.insertString(fb, offset, string, attr);
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    // Filter out spaces when replacing text
                    text = text.replaceAll("\\s", "");
                    super.replace(fb, offset, length, text, attrs);
                }
            });
    }
}
