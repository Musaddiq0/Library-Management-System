package ClientSide;

import objParsing.actionClass;
import objParsing.clientMssg;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
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

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Socket socket;

/**
 * this method makes sure that the user has entered the expected inputs in the text fields provided
 * @param studentID this is the users ID read from the textfield provided
 * @param firstName this is the users first name read from the textfield provided
 * @param lastName  this is the users lastname read from the textfield provided*/
    public boolean sortUserInput(String lastName, String firstName, String studentID){
        //Steps
        //1. Check and confirm the text fields are not empty or contain garbage
        return (firstName.matches("^[A-Za-z]+")) && !(firstName.isBlank()) && (lastName.matches("^[A-Za-z]+")) && !(lastName.isBlank()) && (!(studentID.matches("^[A-Za-z]+")) && (studentID.length() == 10) && !(studentID.isBlank()));

        }

        /**
         * This is the class constructor with the initialization method that builds the login page, it also contains the action listeners for the buttons
         * @param Title this is the firstname of the logged-in user*/
        public UserInterface(String Title) {
        super(Title);
        initGUI();
        reconnectToServer();
        cancelButton.addActionListener(new actionClass());
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Steps
//                1a. Collect and validate  the users input
                if(sortUserInput(lnVal.getText(), fnVal.getText(), studentIdVal.getText())){
//                    Create a student instance to store or destroy upon validation
                    Student student = new Student(Integer.parseInt(studentIdVal.getText()), fnVal.getText(), lnVal.getText());
                    //1b. check the user input match what is expected strings and int respectively
                    loginUser(student);
                }

               else{
                   reconnectToServer();
                   JOptionPane.showMessageDialog(fnVal, "Please enter a valid login details in the spaces provided!!!");
//                   try {
//                       student.destroy();
//                   } catch (Throwable ex) {
//                       throw new RuntimeException(ex);
//                   }

               }
                //2. run the login method with the users inputs collected

            }
        });
    }

    /**This method accepts the student class as the argument and sends the student data for verification
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
            try{
                reply = (clientMssg) objectInputStream.readObject();
                int loginStat = reply.getLoginStatus();
                if (loginStat==0){
                    JOptionPane.showMessageDialog(mainPanel, "Please enter valid login details and try again!!!");
                    potentialStudent = null;
                }
                else{
                    statusLabel.setText(reply.getStatusMssg());
                    loginSuccess(potentialStudent);
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
    /**This method drwas the login page GUI*/
    private void initGUI() {
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setSize(800,800);
        this.setVisible(true);
        this.pack();
        this.addWindowListener( new WindowAdapter(){
        });
    }

    /**This method is used to display the homepage when the user's log-in details are correct*/
    private void loginSuccess(Student curStudent){
        homePage page= new homePage(curStudent);
            page.statusLabelHP.setText("Connection to the server established");
        this.setVisible(false);

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
}
