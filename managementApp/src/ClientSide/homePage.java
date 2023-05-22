package ClientSide;

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
    private JLabel userImgLabel;
    private JPanel userInfoPanel;
    private JLabel sIDTxtLabel;
    private JLabel stdfnamelabel;
    private JLabel stdfnameval;
    private JLabel stdIDval;
    private JLabel stdLnamelabel;
    private JLabel stdLnameval;
    private JLabel usrInfoLabel;
    private JLabel userIconVal;
    private JButton editDetailsButton;
    private JButton homepageButton;
    private JPanel iconValPanel;
    //    protected JPanel booksPanel;
//    protected JPanel categoryPanel;
//    private JPanel BorrowingPanel;
//    String statusLabelHPText = statusLabelHP.getText();
//    UserInterface userInterface = new UserInterface("Library Management App");
    String user;
    String studentID;
    ObjectOutputStream objectOutputStream ;
    ObjectInputStream objectInputStream;
    public homePage(String User, String StudentID){
        super();
        this.user = User;
        this.studentID = StudentID;
        showHomepage();
        userInformationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showUserInfo();
            }
        });
        loginPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginPageSetUp();
            }
        });
        borrowPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showBorrowPage();
            }
        });
    }


    private Socket socket;
    private void showHomepage() {
        reconnectToServer();
        this.setContentPane(homepageMainPanel);
        userInfoPanel.setVisible(false);
        welcomLabel.setText("Welcome to the Library application " +this.user);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setSize(800,800);
        this.setVisible(true);
        this.pack();
        this.addWindowListener( new WindowAdapter(){
        });
    }
    public void showBorrowPage(){
        this.setVisible(false);
        BorrowPage borrowPage =new BorrowPage(this.user, this.studentID);
        borrowPage.initBorrowPage();

    }
    private void showUserInfo(){
        if(objectOutputStream != null && objectInputStream!=null){
            //        steps
//       1. pass the studentID to the database to find out the details of the user
            String studentID  = this.studentID;
            try{
//                writing to the server
                objectOutputStream.writeObject(new clientMssg(clientMssg.clientCommands.UserInfoParcel,studentID));
                System.out.println("packet sent to server");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            creating the parcel to receive the response from the server side
            Student student = null;
            serverResponse serverResponse = null;

//            List <Object> studentInfo = new ArrayList<>();
            try {
//                receiving the response from the server
                student = (Student) objectInputStream.readObject();
                String sqlCode = student.getStatus();
//                the user details are arranged like this [studentID,FirstName,LastName,Number of borrowed books]
//                studentInfo = response.getUserInfoParcels();
//                reading the serve response for the student object
                if(!sqlCode.isEmpty()){
                    stdIDval.setText(String.valueOf(student.getStudentID()));
                    stdfnameval.setText(student.getFirstName());
                    stdLnameval.setText(student.getLastName());
                    setupUIHomePage(student.getStatus());
                }
//                reading the response for the server response
                else{
                    usrInfoLabel.setText(student.getStatus());
                }
//                display the panel and update the GUI accordingly
//                setupUIHomePage(student.getStatus());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
//        steps
//       1. create a student class object


    }
    private void loginPageSetUp(){
        UserInterface userInterface = new UserInterface(homePage.super.getTitle());
        userInterface.setVisible(true);
        this.setVisible(false);
    }

    private void setupUIHomePage(String sqlCode){
        userInfoPanel.setVisible(true);
        homepageButtonsPanel.setVisible(false);
        usrInfoLabel.setText(sqlCode);
        ImageIcon userIcon = new ImageIcon("profile-icon.jpeg");
        userImgLabel.setIcon(userIcon);
        homepageMainPanel.updateUI();
    }


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
     homePage page = new homePage("","");

    }
}
