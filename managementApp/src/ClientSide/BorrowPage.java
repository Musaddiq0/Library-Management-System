package ClientSide;

import objParsing.GenericTableModel;
import objParsing.TableResponseContainer;
import objParsing.clientMssg;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    /**Initial page that runs when the user signs in and clicks on the borrow button from the homePage
     * @param userSigned this is the user that has signed in to the system*/
    public BorrowPage (String userSigned){
        reconnectToServer();
        this.setTitle(userSigned);
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(sortUserInput(authorVal.getText(), titleVal.getText())){
                    checkForBooks(authorVal.getText(), titleVal.getText());
                }else{
                    borrowMainStatusLb.setText("The text field contains some error please check and try again ");
                }
            }
        });
        borrowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(booksTable.getSelectedRow() != -1){
                  Object userInput =  printSelectedRow((GenericTableModel) booksTable.getModel());
                    borrowBook(userInput);
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

    public boolean sortUserInput(String author, String title){
        if((!author.isBlank()) && (!title.isBlank())){
            return true;
        }
        else return (author.matches("^[A-Za-z]+")) && (title.matches("^[A-Za-z]+"));
    }

    public synchronized void checkForBooks(String author, String title) {
        if (objectInputStream != null && objectOutputStream!=null){
                String userInput = String.format("%s:%s:%s",authorVal.getText(), titleVal.getText(), this.getTitle());
//                Sending the user input to the server
                try {
                    objectOutputStream.writeObject(new clientMssg(clientMssg.clientCommands.VIEWBOOKSUSINGAT, userInput));
                } catch (IOException e) {
                    borrowMainStatusLb.setText("IOException " + e);
                }
//                Receiving the response from the client to update the GUI Table on the borrow page
                TableResponseContainer responseContainer;
                try {
                    responseContainer = (TableResponseContainer) objectInputStream.readObject();
                    String sqlResultStat = responseContainer.getStatus();
                    if(Objects.isNull(sqlResultStat)||sqlResultStat.isBlank()){
                        borrowMainStatusLb.setText("No Results for" +author + " and book " +title );
                        booksTable.setModel(new GenericTableModel(responseContainer.columns, responseContainer.data));
//                        if(booksTable.getRowSelectionAllowed())
                    }else{
                        borrowMainStatusLb.setText(responseContainer.getStatus());
                        booksTable.setModel(new GenericTableModel(responseContainer.columns, responseContainer.data));
                        borrowLabelInstr.setText("Please select the book you want to borrow then click the borrow button");
                        bBtnPanel.setVisible(true);
                        bBtnPanel.updateUI();
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

    public void borrowBook(Object userInput){
        if(objectInputStream != null && objectOutputStream != null){
            // Steps
            // 1. send the userInput to the server
            try{
                objectOutputStream.writeObject(new clientMssg(clientMssg.clientCommands.BORROWBOOK, userInput));

            }catch (IOException e){
                borrowMainStatusLb.setText("IOException " + e);
            }
            // 2
        }
    }
    public Object printSelectedRow(GenericTableModel model) {
        List<Object> mixedValues = new ArrayList<>();
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow != -1) {
            for (int column = 0; column < model.getColumnCount(); column++) {
                Object value = model.getValueAt(selectedRow, column);
                mixedValues.add(value);
                System.out.println("Value at column " + column + ": " + value);
                System.out.println(mixedValues);
                System.out.println(this.getTitle());
            }
            mixedValues.add(this.getTitle());
            System.out.println(mixedValues);
        } else {
            System.out.println("No row is selected");
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
}
