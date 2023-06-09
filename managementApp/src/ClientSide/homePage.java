package ClientSide;
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
        userInformationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showUsrInforPage(student);
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
