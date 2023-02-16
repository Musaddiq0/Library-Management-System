package ServerSide;

import objParsing.TableResponseContainer;
import objParsing.clientMssg;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class threadHandler implements  Runnable {

    private final Socket socket;
    private static int connectionCount = 0;
    private final int threadNo;
    ObjectInputStream objectInputStream;
    ObjectOutputStream   objectOutputStream;

    public threadHandler(Socket socket) throws IOException {
        this.socket = socket;
        connectionCount++;
        threadNo = connectionCount;
        threadCount("Connection " + threadNo + " established" );
    }

    private  void threadCount (String count){
        System.out.println("threadHandler " + threadNo + ": " + count);
    }

    @Override
    public void run() {
        try{
            // reading the clients requests and handling exceptions
            threadCount("Waiting for data from a client thread...");
            System.out.println("Server: Waiting for data from client...");
            clientMssg objParsing;

             objectInputStream = new ObjectInputStream(socket.getInputStream());
             objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            while((objParsing = (clientMssg) objectInputStream.readObject()) != null ){
                //reading the clients msg and storing the read values
                System.out.println(objParsing.getCommands());
                String clientSays = objParsing.getUserInput();


                //checking the commands and performing the actions respectively
                if(objParsing.getCommands() == clientMssg.clientCommands.LOGIN){
                    String[] loginDetails = clientSays.split(":");
                    for(int i =0; i<loginDetails.length; i++){
                        loginDetails[i]=loginDetails[i].trim();
                    }
                    String idSQLCmd = "SELECT COUNT() FROM Users WHERE StudentID=?";
                    String fnSQLCmd = "SELECT COUNT() FROM Users WHERE FirstName=?";
                    String lnSQLCmd = "SELECT COUNT() FROM Users WHERE LastName=?";
//                        preparedStatement.setString(2,loginDetails[1]);
//                        preparedStatement.setString(3,loginDetails[2]);
                    ArrayList<Integer> sqlresult = new ArrayList<>(); // Array list to store the result from DB for all SQL commands
                    try(Connection connection = sqlConn.getConnected()){
                        System.out.println("Connected to the DB");
                        //checking if the ID exists on the table
                        PreparedStatement idPreparedStatement = connection.prepareStatement(idSQLCmd);
                        idPreparedStatement.setString(1,loginDetails[0]);
                        ResultSet idResult = idPreparedStatement.executeQuery();
                        int idExist = Integer.parseInt(idResult.getString(1));
                        sqlresult.add(idExist);
                        //checking if the entered firstname exists
                        PreparedStatement fnPreparedStatement = connection.prepareStatement(fnSQLCmd);
                        connection.prepareStatement(fnSQLCmd);
                        fnPreparedStatement.setString(1,loginDetails[2]);
                        ResultSet fnResult = fnPreparedStatement.executeQuery();
                        int fnExist = fnResult.getInt(1);
                        sqlresult.add(fnExist);
                        //checking if the entered lastname exists and corresponds
                        PreparedStatement lnPreparedStatement = connection.prepareStatement(lnSQLCmd);
                        connection.prepareStatement(lnSQLCmd);
                        lnPreparedStatement.setString(1,loginDetails[1]);
                        ResultSet lnResult = lnPreparedStatement.executeQuery();
                        int lnExist = lnResult.getInt(1);
                        sqlresult.add(lnExist);
                        //checking if the list contain a zero to identify an invalid login
                        if(sqlresult.contains(0)){ //if the users doesn't exist
                            objParsing.setStatusMssg("User does not exist");
                            objParsing.setLoginStatus(0);
                            objectOutputStream.writeObject(objParsing);
                        }

                        else{
                            objParsing.setLoginStatus(1);
                            objParsing.setStatusMssg("Welcome " + loginDetails[1]);
                            objectOutputStream.writeObject(objParsing);
                        }
                    }
                }
                else if(objParsing.getCommands() == clientMssg.clientCommands.VIEWBOOKSUSINGAT){
//                    steps
//                    1. sort the user inputs the  user input into an array
                    String[]userInput = clientSays.split(":");
//                    2. create a query to return all the books with that title and author
                    String returnBooksQuery = "SELECT * FROM Books WHERE Author = ? AND Title = ? ";
                    try (Connection connection = sqlConn.getConnected()) {
                        System.out.println("Connected to the DB");
                        //Pass the title and author in the SQL command to return the lists of book(s) matching the selections
                        PreparedStatement preparedStatement = connection.prepareStatement(returnBooksQuery);
                        preparedStatement.setString(1, userInput[0]);
                        preparedStatement.setString(2, userInput[1]);
                        //store the query result in a resultset
                        ResultSet viewBooksResult = preparedStatement.executeQuery();
                        List<List<Object>> booksResult = new ArrayList<>();
                        List<Object> tableCols = new ArrayList<>();
                        while (viewBooksResult.next()) {
                            tableCols.add(viewBooksResult.getInt(1));
                            tableCols.add(viewBooksResult.getString(2));
                            tableCols.add(viewBooksResult.getString(3));
                            tableCols.add(viewBooksResult.getInt(4));
                            booksResult.add(tableCols);
                        }
//                        List<String> cols = null;
                        String SQLstatusUpdate;
                        if (!booksResult.isEmpty()) {
                            SQLstatusUpdate = "Below are the results of books written by the author " +userInput[0];
                            TableResponseContainer responseContainer = new TableResponseContainer(booksCols, booksResult, SQLstatusUpdate);
                            objectOutputStream.writeObject(responseContainer);
                            System.out.println(Arrays.toString(booksCols));
                        } else {
                            SQLstatusUpdate = "No such record, please try a different title and author";
                            TableResponseContainer responseContainer = new TableResponseContainer(booksCols, booksResult, SQLstatusUpdate);
                            responseContainer.setStatus(SQLstatusUpdate);
                            objectOutputStream.writeObject(responseContainer);
                            System.out.println(Arrays.toString(booksCols));
                        }
                        //create a structure to handle the result to be displayed on the table in the GUI
                    }
                }
                else if (objParsing.getCommands() == clientMssg.clientCommands.BORROWBOOK) {
                    //Steps
                    // 1. Get the Object containing the selected rows
                    Object booksSelected = new ArrayList<>();
                    booksSelected = objParsing.getBooksSelected();
                    //2. update the borrows table with the student details and the book borrowed
                    //2a. create the SQL query to insert into the borrow table with the user and books information
                    String updateBorrowTable = "Insert into borrow where";
                }
            }
        }catch (IOException ex){
            Logger.getLogger(threadHandler.class.getName()).log(Level.SEVERE,null,ex);
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }finally {
            try{
                threadCount("We have lost connection to the client " + threadNo + ".");
                socket.close();
            }catch (IOException ex){
                Logger.getLogger(threadHandler.class.getName()).log(Level.SEVERE,null,ex);
            }
        }


    }

    String[] booksCols = {"ISBN", "Title", "Author", "Quantity"};
    String[] borrowedCol ={"",""};

}
