package ClientSide;

import objParsing.clientMssg;

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

public class UserInfor extends JFrame{
    private JLabel imageLabel;
    private JLabel fnameVal;
    private JLabel lnameVal;
    private JPanel mainPanel;
    private JLabel sIDval;
    private JLabel statLabel;
    private JButton homePageBt;
    private JLabel borrowedBooksVal;
    Student signStudent;
  // Variables used for the server and client communication
    Socket socket;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;

/**This is the only constructor for this class, and it accepts a student class as the argument
 * @param student this is class containing the students information when the user signs in before getting to this section
 **/
    public UserInfor(Student student){
        super();
        this.signStudent = student;
        showUsrInfo();
        homePageBt.addActionListener(new ActionListener() {
            /**
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                showHomepage(signStudent);

            }
        });
    }

    /**This is the methods that sets the properties of the GUI to be displayed. It also contains the connect to server method.
     **/
    public void showUsrInfo() {
//        Steps
//        1. connect to the server
        reconnectToServer();
//        2. set the size of the main panel and set the main settings of the GUI for User Information page
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setTitle("Welcome to the Library Application");
//        setting up the user information GUI
        ImageIcon userIcon = new ImageIcon("profileicon.jpeg");
        imageLabel.setIcon(userIcon);
        fnameVal.setText(signStudent.getFirstName());
        lnameVal.setText(signStudent.getLastName());
        sIDval.setText(String.valueOf(signStudent.getStudentID()));
        borrowedBooksVal.setText(getBorrowedBooksNumber(signStudent));
        this.pack();
        this.update(imageLabel.getGraphics());
        this.addWindowListener( new WindowAdapter(){
        });
    }

    public synchronized String  getBorrowedBooksNumber(Student currentStudent){
//        Steps
//        1. create variables to return after server communication
        String borrowedBooksCount = "";
        if (objectInputStream != null && objectOutputStream != null){
//        2. using try and catch send data
            try {
                objectOutputStream.writeObject(new clientMssg(clientMssg.clientCommands.GETNUMBORROWBOOKS, currentStudent));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//        3. read the reply from the user to update the GUI to display information properly
            try {
                signStudent = (Student) objectInputStream.readObject();
                borrowedBooksCount = String.valueOf(signStudent.getNoBorrowedBooks());
//                sqlStatus = signStudent.getSqlStatus();
                if(signStudent.getSqlStatus() == null){
//                    signStudent = updateStudent;
                    return borrowedBooksCount;
                }
                else return signStudent.getSqlStatus();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }else{
            statLabel.setText("Connection to server not established !!!");
        }
        return borrowedBooksCount;
    }
//    private void showUserInfo(){
//        if(objectOutputStream != null && objectInputStream!=null){
//            //        steps
////       1. pass the studentID to the database to find out the details of the user
//            int studentID = signStudent.getStudentID();
//            try{
////                writing to the server
//                objectOutputStream.writeObject(new clientMssg(clientMssg.clientCommands.GETUSERINFORMATION,studentID));
//                System.out.println("packet sent to server");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
////            creating the parcel to receive the response from the server side
//            Student student = null;
//            serverResponse serverResponse = null;
////            List <Object> studentInfo = new ArrayList<>();
//            try {
////                receiving the response from the server
//                student = (Student) objectInputStream.readObject();
//                String sqlCode = student.getSqlStatus();
////                the user details are arranged like this [studentID,FirstName,LastName,Number of borrowed books]
////                studentInfo = response.getUserInfoParcels();
////                reading the serve response for the student object
//                if(!sqlCode.isEmpty()){
//                    fnameVal.setText(student.getFirstName());
//                    lnameVal.setText(student.getLastName());
//                    sIDval.setText(String.valueOf(student.getStudentID()));
//                    setupUIHomePage(student.getSqlStatus());
//                }
////                reading the response for the server response
//                else{
//                    usrInfoLabel.setText(student.getSqlStatus());
//                }
////                display the panel and update the GUI accordingly
////                setupUIHomePage(student.getStatus());
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//        }
////        steps
////       1. create a student class object
//
//    }
    public void reconnectToServer() {
        endConnection();
        statLabel.setText("Status: Attempting connection to server");
        try {
            socket = new Socket("127.0.0.1", 2000);

            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            statLabel.setText("Status: Connected to server");
        } catch (IOException ex) {
            Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
            statLabel.setText(ex.toString()); // connection failed
        }
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
    public void showHomepage (Student signedStudent){
        homePage homePage = new homePage(signedStudent);
        this.setVisible(false);
        homePage.setVisible(true);
    }
//    showing the user Pic section.
//    private void setupUIHomePage(String sqlCode){
//        userInfoPanel.setVisible(true);
//        homepageButtonsPanel.setVisible(false);
//        usrInfoLabel.setText(sqlCode);
//        ImageIcon userIcon = new ImageIcon("profile-icon.jpeg");
//        userImgLabel.setIcon(userIcon);
//        homepageMainPanel.updateUI();
//    }
}
