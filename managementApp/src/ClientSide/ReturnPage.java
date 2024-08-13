package ClientSide;

import objParsing.GenericTableModel;
import objParsing.TableResponseContainer;
import objParsing.clientMssg;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReturnPage extends JFrame{
    private JPanel ReturnPanel;
    private JButton homePageButton;
    private JButton returnButton;
    private JTable borrowedBooksTable;
    private JLabel returnGreeting;
    public JLabel connectionMssg;

    Student student;
    ObjectOutputStream objectOutputStream ;
    ObjectInputStream objectInputStream;
    GenericTableModel tableModel;
    private Socket socket;

    public ReturnPage(Student loginStudent){
        super();
        this.student = loginStudent;
        borrowedBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showBorrowedBooks(loginStudent);

        returnButton.addActionListener(new ActionListener() {
            /**
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
//                make sure there is a selection on the table
                if(borrowedBooksTable.getSelectedRow() !=-1){
                    List<Object> returnRequest = returnRequestData((GenericTableModel) borrowedBooksTable.getModel(),borrowedBooksTable);
                    returnRequest.add(loginStudent.getStudentID());
                    returnBook(loginStudent,returnRequest);
                }
                else{
                    connectionMssg.setText("Please select a book to return!!!");
                }

            }
        });
        homePageButton.addActionListener(new ActionListener() {
            /**
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                showHomePage(student);

            }
        });
    }

    private void showHomePage(Student curStudent){
        homePage homePage= new homePage(curStudent);
        homePage.statusLabelHP.setText("Connection to the server established");
        this.setVisible(false);

    }


    private synchronized void showBorrowedBooks(Student student) {
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
                    connectionMssg.setText(responseContainer.getStatus());
//                        set the table model to display the results from the database
                    borrowedBooksTable.setModel(new GenericTableModel(responseContainer.columns, responseContainer.data));
                    ReturnPanel.updateUI();
                }
                else{
                    connectionMssg.setText(responseContainer.getStatus());
                }
            }catch (IOException e ){
                connectionMssg.setText("IOE exception occurred" + e);
            } catch (ClassNotFoundException e) {
                connectionMssg.setText("ClassNotFoundException occurred" + e);
            }

        }
    }

    private synchronized void returnBook(Student student, List<Object>returnData) {
        if(objectInputStream != null && objectOutputStream!= null){
//Steps
//            1.  send the users selection from the table
            try{
                clientMssg clientMssg = new clientMssg(objParsing.clientMssg.clientCommands.BORROWBOOK);
                clientMssg.setBooksSelected(returnData);
                clientMssg.setStudParcels(student);
                objectOutputStream.writeObject(clientMssg);
            } catch (IOException |RuntimeException e) {
                connectionMssg.setText(e.getLocalizedMessage());
            }
//            2. read the reply from the server
            TableResponseContainer tableResponseContainer =null;
            try {
                tableResponseContainer = (TableResponseContainer) objectInputStream.readObject();
                if(tableResponseContainer.getBorrowedCode() == 0){
                    borrowedBooksTable.setModel(new GenericTableModel(tableResponseContainer.columns, tableResponseContainer.data));
                }
                else{
                    connectionMssg.setText(tableResponseContainer.getStatus());
                    borrowedBooksTable.setVisible(false);
                }

            }
            catch (ClassNotFoundException e) {
                connectionMssg.setText("Class not found exception " +e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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
    public void reconnectToServer() {
        endConnection();
        connectionMssg.setText("Status: Attempting connection to server");
        try {
            socket = new Socket("127.0.0.1", 2000);

            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            connectionMssg.setText("Status: Connected to server");
        } catch (IOException ex) {
            Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
            connectionMssg.setText(ex.toString()); // connection failed
        }
    }

    public List<Object> returnRequestData(GenericTableModel tableModel, JTable booksTable){
        int selectedRow = booksTable.getSelectedRow();
        List<Object> userInput = new ArrayList<>();
        if (selectedRow != -1) {
            for (int column = 0; column < tableModel.getColumnCount(); column++) {
                Object value = tableModel.getValueAt(selectedRow, column);
                userInput.add(value);
            }
            System.out.println(userInput);
        }
        else {
            return  userInput;
        }
        return userInput;
    }

}
