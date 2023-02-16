package ClientSide;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class homePage  extends JFrame{

    protected JPanel homepageMainPanel;
    protected JPanel UsrInfoHp;
    JLabel hpWelcomeLB;
    private JButton userInformationButton;
    private JButton borrowPageButton;
    private JLabel welcomLabel;
    JLabel statusLabelHP;
    private JButton loginPageButton;
//    protected JPanel booksPanel;
//    protected JPanel categoryPanel;
//    private JPanel BorrowingPanel;
//    String statusLabelHPText = statusLabelHP.getText();
//    UserInterface userInterface = new UserInterface("Library Management App");
    String user;
    ObjectOutputStream objectOutputStream ;
    ObjectInputStream objectInputStream;
    public homePage(String User){
        super(User);
        this.user = User;
        homepageInitGUI();
        userInformationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initUserInfo();
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
    private void homepageInitGUI() {
        reconnectToServer();
        this.setContentPane(homepageMainPanel);
        welcomLabel.setText("Welcome to the Library application " +this.user);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setSize(250, 150);
        this.setVisible(true);
        this.pack();
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e); //To change body of generated methods, choose Tools | Templates.
                endConnection();
                System.exit(0);
            }
        });
    }
    public void showBorrowPage(){
        this.setVisible(false);
        BorrowPage borrowPage =new BorrowPage(this.user);
        borrowPage.initBorrowPage();

    }
    private void initUserInfo(){
        JLabel ppLabel = new JLabel(new ImageIcon("/Users/mussy/Documents/LibProject/profileicon.jpeg"));
        JLabel studentID = new JLabel();
        studentID.setText("Student ID: ");
        JLabel firstName = new JLabel();
        firstName.setText("Name:");
        JLabel lastName = new JLabel();
        lastName.setText("Surname: ");
        ppLabel.add(UsrInfoHp);
        firstName.add(UsrInfoHp);
        lastName.add(UsrInfoHp);
        hpWelcomeLB.setText("Your Information is displayed below");
        UsrInfoHp.setVisible(true);

    }
    private void loginPageSetUp(){
        UserInterface userInterface = new UserInterface(homePage.super.getTitle());
        userInterface.setVisible(true);
        this.setVisible(false);
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
     homePage page = new homePage("");

    }
}
