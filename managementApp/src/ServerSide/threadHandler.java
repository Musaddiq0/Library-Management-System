package ServerSide;

import ClientSide.Student;
import objParsing.TableResponseContainer;
import objParsing.clientMssg;
import objParsing.serverResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class threadHandler implements  Runnable {

    private final Socket socket;
    private static int connectionCount = 0;
    private final int threadNo;
    ObjectInputStream objectInputStream;
    ObjectOutputStream   objectOutputStream;
    String[] booksCols = {"ISBN", "Title", "Author", "Quantity"};
    String[] BorroweMenuTablecols = {"ISBN", "Title", "Author"};

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
                Student studParcels;
                String clientSays = objParsing.getUserInput();
                studParcels = (Student) objParsing.getStudParcels();
                List <Object>  borrowBooksInfo = objParsing.getBooksSelected();


                //checking the commands and performing the actions respectively
                if(objParsing.getCommands() == clientMssg.clientCommands.LOGIN){
//                    steps
//                    1. get the student details name, stdID and lastname
                    int studentID = studParcels.getStudentID();
                    String fname = studParcels.getFirstName();
                    String lsname = studParcels.getLastName();
//                  create the sql commands to send to the server
                    String idSQLCmd = "SELECT COUNT() FROM Users WHERE StudentID=?";
                    String fnSQLCmd = "SELECT COUNT() FROM Users WHERE FirstName=?";
                    String lnSQLCmd = "SELECT COUNT() FROM Users WHERE LastName=?";
//                        preparedStatement.setString(2,loginDetails[1]);
//                        preparedStatement.setString(3,loginDetails[2]);
                    // Array list to store the result from DB for all SQL commands
                    ArrayList<Integer> sqlresult = new ArrayList<>();
                    try(Connection connection = ServerSide.sqlConn.getConnected()){
                        System.out.println("Connected to the DB");
                        //checking if the ID exists on the table
                        PreparedStatement idPreparedStatement = connection.prepareStatement(idSQLCmd);
                        idPreparedStatement.setInt(1,studentID);
                        ResultSet idResult = idPreparedStatement.executeQuery();
                        int idExist = Integer.parseInt(idResult.getString(1));
                        sqlresult.add(idExist);
                        //checking if the entered firstname exists
                        PreparedStatement fnPreparedStatement = connection.prepareStatement(fnSQLCmd);
                        connection.prepareStatement(fnSQLCmd);
                        fnPreparedStatement.setString(1,fname);
                        ResultSet fnResult = fnPreparedStatement.executeQuery();
                        int fnExist = fnResult.getInt(1);
                        sqlresult.add(fnExist);
                        //checking if the entered lastname exists and corresponds
                        PreparedStatement lnPreparedStatement = connection.prepareStatement(lnSQLCmd);
                        connection.prepareStatement(lnSQLCmd);
                        lnPreparedStatement.setString(1,lsname);
                        ResultSet lnResult = lnPreparedStatement.executeQuery();
                        int lnExist = lnResult.getInt(1);
                        sqlresult.add(lnExist);
                        //checking if the list contain a zero to identify an invalid login

                    }
                    if(sqlresult.contains(0)){ //if the users doesn't exist
                        objParsing.setStatusMssg("User does not exist");
                        objParsing.setLoginStatus(0);
                        objectOutputStream.writeObject(objParsing);
                    }

                    else{
                        objParsing.setLoginStatus(1);
                        objParsing.setStatusMssg("Welcome " + fname);
                        objectOutputStream.writeObject(objParsing);
                    }
                }
                else if(objParsing.getCommands() == clientMssg.clientCommands.CHECKFORBOOKUSERINPUT){
//                    steps
//                    1. sort the user inputs the  user input into an array
                    String[]userInput = clientSays.split(":");
                    String returnBooksQuery =null;
                    if(Objects.equals(userInput[0], "Title only")){
                        returnBooksQuery = getTitleSQL();
//                        establish the connection to the DB
                        try (Connection connection =  ServerSide.sqlConn.getConnected()) {
                            System.out.println("connection to DB established");
//                            pass the query to excute
                            PreparedStatement preparedStatement = connection.prepareStatement(returnBooksQuery);
                            preparedStatement.setString(1, userInput[1]);
//                            store the query results in a ResultSet if any
                            ResultSet viewBooksResult = preparedStatement.executeQuery();
//                            setting up the table data structure (rows and columns )
                            List<List<Object>> booksResult = new ArrayList<>();
                            List<Object> tableCols = new ArrayList<>();
                            while (viewBooksResult.next()) {
                                tableCols.add(viewBooksResult.getInt(1));
                                tableCols.add(viewBooksResult.getString(2));
                                tableCols.add(viewBooksResult.getString(3));
                                tableCols.add(viewBooksResult.getInt(4));
                                booksResult.add(tableCols);
                            }
//                            sending back to the server to display
                            List<String> cols = new ArrayList<>(Arrays.asList(booksCols));
                            String SQLstatusUpdate;
                            if (!booksResult.isEmpty()){
                                SQLstatusUpdate = "Below are the results of books marching title " +userInput[0];
                                TableResponseContainer responseContainer = new TableResponseContainer(cols, booksResult, SQLstatusUpdate);
                                objectOutputStream.writeObject(responseContainer);
                                System.out.println(cols);
                            }
                            else {
                                SQLstatusUpdate = "No such record, please try a different title and author";
                                TableResponseContainer responseContainer = new TableResponseContainer(cols, booksResult, SQLstatusUpdate);
                                responseContainer.setStatus(SQLstatusUpdate);
                                objectOutputStream.writeObject(responseContainer);
                                System.out.println(cols);
                            }
                        }
                    }
                    else if(Objects.equals(userInput[0], "Author only")) {
                        returnBooksQuery = getAuthorSQL();
//                        establish the connection to the DB
                        try (Connection connection =  ServerSide.sqlConn.getConnected()) {
                            System.out.println("connection to DB established");
//                            pass the query to excute
                            PreparedStatement preparedStatement = connection.prepareStatement(returnBooksQuery);
                            preparedStatement.setString(1, userInput[1]);
//                            store the query results in a ResultSet if any
                            ResultSet viewBooksResult = preparedStatement.executeQuery();
//                            setting up the table data structure (rows and columns )
                            List<List<Object>> booksResult = new ArrayList<>();
                            List<Object> tableCols = new ArrayList<>();
                            while (viewBooksResult.next()) {
                                tableCols.add(viewBooksResult.getInt(1));
                                tableCols.add(viewBooksResult.getString(2));
                                tableCols.add(viewBooksResult.getString(3));
                                tableCols.add(viewBooksResult.getInt(4));
                                booksResult.add(tableCols);
                            }
//                            sending back to the server to display
                            List<String> cols = new ArrayList<>(Arrays.asList(booksCols));
                            String SQLstatusUpdate;
                            if (!booksResult.isEmpty()){
                                SQLstatusUpdate = "Below are the results of books marching title " +userInput[1];
                                TableResponseContainer responseContainer = new TableResponseContainer(cols, booksResult, SQLstatusUpdate);
                                objectOutputStream.writeObject(responseContainer);
                                System.out.println(Arrays.toString(booksCols));
                            }
                            else {
                                SQLstatusUpdate = "No such record, please try a different title and author";
                                TableResponseContainer responseContainer = new TableResponseContainer(cols, booksResult, SQLstatusUpdate);
                                responseContainer.setStatus(SQLstatusUpdate);
                                objectOutputStream.writeObject(responseContainer);
                                System.out.println(Arrays.toString(booksCols));
                            }
                        }
                    }
                   // todo 2 check for when when any is blank and then create a condition that fits only that
                    else{
                        try (Connection connection =  ServerSide.sqlConn.getConnected()) {
                            returnBooksQuery = getbooksAllSQL();
                            System.out.println("connection to DB established");
//                            pass the query to execute
                            PreparedStatement preparedStatement = connection.prepareStatement(returnBooksQuery);
                            preparedStatement.setString(1, userInput[1]);
                            preparedStatement.setString(2,userInput[2]);
//                            store the query results in a ResultSet if any
                            ResultSet viewBooksResult = preparedStatement.executeQuery();
//                            setting up the table data structure (rows and columns )
                            List<List<Object>> sqlResultsFromDB = new ArrayList<>();
                            while (viewBooksResult.next()) {
                                List<Object> tableCols = new ArrayList<>();
                                tableCols.add(viewBooksResult.getInt(1));
                                tableCols.add(viewBooksResult.getString(2));
                                tableCols.add(viewBooksResult.getString(3));
                                tableCols.add(viewBooksResult.getInt(4));
                                sqlResultsFromDB.add(tableCols);
                            }
//                           sending back to the server to display
                            List<String> cols = new ArrayList<>(Arrays.asList(booksCols));
                            String SQLstatusUpdate;
                            if (!sqlResultsFromDB.isEmpty()){
                                SQLstatusUpdate = "Below is the result of your search ";
                                TableResponseContainer responseContainer = new TableResponseContainer(cols, sqlResultsFromDB, SQLstatusUpdate);
                                objectOutputStream.writeObject(responseContainer);
                                System.out.println(Arrays.toString(booksCols));
                            }
                            else {
                                SQLstatusUpdate = "No such record, please try a different title and author";
                                TableResponseContainer responseContainer = new TableResponseContainer(cols, sqlResultsFromDB, SQLstatusUpdate);
                                responseContainer.setStatus(SQLstatusUpdate);
                                objectOutputStream.writeObject(responseContainer);
                                System.out.println(Arrays.toString(booksCols));
                            }
                        }
                    }

                }
                else if (objParsing.getCommands() == clientMssg.clientCommands.BORROWBOOK) {
                    //Steps
                    // 1. Get the input sent from the UI and store for processing
                    StringBuilder builder = new StringBuilder();
// concatenating the user input to one string to send to the server
                    for (Object str : borrowBooksInfo) {
                        builder.append(String.format("%s:", str));
                    }
                    String sortedInput = builder.toString();
                    if (!sortedInput.isEmpty()) {
                        sortedInput = sortedInput.substring(0, sortedInput.length() - 1); // Remove the trailing colon
                    }
//                    split the sorted client details and store in an array
                    String[] userInput = sortedInput.split(":");
                    Date date = objParsing.getReturnDate();
//                    convert the date to SQL date format
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
//                  set the SQL query
                    String sql = "INSERT INTO Borrow (ISBN, Title, Author, ReturnDate, BorrowID, StudentID, FirstName) VALUES (?, ?, ?, ?, ?, ?, ?)";
//                    Create the SQL connection
                    try(Connection connect = sqlConn.getConnected()){
                        System.out.println("connection to DB established");
//                        update the borrow table on the SQL to reflect the user request
                        PreparedStatement preparedStatement = connect.prepareStatement(sql);
                        preparedStatement.setInt(1, Integer.parseInt(userInput[2].trim())); //ISBN
                        preparedStatement.setString(2,userInput[3].trim());  //Title
                        preparedStatement.setString(3, userInput[5].trim()); //Author
                        preparedStatement.setDate(4, (sqlDate)); //ReturnDate
                        preparedStatement.setInt(5, Integer.parseInt(userInput[4].trim())); //BorrowID,
                        preparedStatement.setInt(6, Integer.parseInt(userInput[0].trim())); //StudentID
                        preparedStatement.setString(7,userInput[1].trim()); //Firstname
//                        store the query result code
                        int sqlStatus = preparedStatement.executeUpdate();
                        String dbResponse = null;
                        serverResponse response = new serverResponse();
                        //2. update the GUI with the results from the action performed with the student details and the book borrowed
                        if (sqlStatus >0){
                            dbResponse = "Please proceed to the counter to collect your book. Enjoy have a great ";
                            response.setBorrowFlag(sqlStatus);
                            response.setMssgToDisplay(dbResponse);
                            objectOutputStream.writeObject(response);
                            System.out.println(dbResponse+ " plus students parcel");
////                            update the count of the available book
////                            reduce the count by 1 after every borrow
//                            String updateBooksCount = "SELECT Quantity FROM Books WHERE ISBN =?";
//
//                             preparedStatement = connect.prepareStatement(updateBooksCount);
//                             preparedStatement.setInt(1,Integer.parseInt(userInput[2].trim()));
//                             ResultSet resultSet = preparedStatement.executeQuery();
//                             int dbBookCount = resultSet.getInt(1);
//                             if(dbBookCount != 0 ){
//                                 dbBookCount =-1;
//
//                                 String updateCount = " "
//                             }


                        }
                        else{
                            dbResponse = "Something wong !!! try again";
//                            set the server response parcels to send back to the GUI
                            response.setBorrowFlag(sqlStatus);
                            response.setMssgToDisplay(dbResponse);
                            objectOutputStream.writeObject(response);
                            System.out.println(dbResponse);
                        }
                    }
                    //2a. create the SQL query to insert into the borrow table with the user and books information
                }
//                else  if(objParsing.getCommands() == clientMssg.clientCommands.UserInfoParcel){
////                    steps
////                    1. Reading the parcel
//                    // convert to an integer to send to server.
//                    int stdIDint = Integer.parseInt(clientSays);
////                    open connection to the SQL to receive show the user info
//                    try(Connection connect = sqlConn.getConnected()){
//                        System.out.println("connection to DB established");
////                        prepare and parse the ID
//                        String sqlQuery = "Select * FROM  Users WHERE studentID = ?";
//                        PreparedStatement preparedStatement = connect.prepareStatement(sqlQuery);
//                        preparedStatement.setInt(1,stdIDint);
////                        query result in a resultset
//                        ResultSet userInforSqlresults = preparedStatement.executeQuery();
////                        List<Object> userInfoDetails = new ArrayList<>();
//                        String firstName="";
//                        String lastname="";
//                        int studentID=0;
//                        boolean check = false;
//                        serverResponse response =new serverResponse();
//                        while(userInforSqlresults.next()){
////                            List<Object> datafromDB = new ArrayList<>();
//                            studentID = (userInforSqlresults.getInt(1));
//                            firstName =(userInforSqlresults.getString(2));
//                            lastname= (userInforSqlresults.getString(3));
////                            userInfoDetails.add(datafromDB);
////                            Student student = new Student(firstName,lastname,studentID);
//                            response.setSqlcode("Got result");
//                            check = true;
//                        }
////                        Steps
////                        1. send back the result if positive
//                        if(check){
////                            create a new object of the serverResponse class with the DB result passed in
//                            Student updateStudent = new Student(studentID,firstName,lastname);
//                            updateStudent.setSqlStatus("got result");
//                            response.setSqlcode(updateStudent.getSqlStatus());
//                            //sending the result back to the GUI
//                            objectOutputStream.writeObject(updateStudent);
//                        }
////                        2. else send back the result if negative
//                        else {
//                            response.setSqlcode("No result");
//                            //sending the result back to the GUI
//                           objectOutputStream.writeObject(response);
//                        }
//
//
//
//
//                    }
//                }
                else if (objParsing.getCommands() == clientMssg.clientCommands.GETNUMBORROWBOOKS) {
                    int noBorrowBooks = 0;
                    boolean checkbit = false;
//                        the sql code to execute
                    String dbRequest = " SELECT COUNT(*) AS no_of_borrowed_books FROM Borrow WHERE StudentID = ?; ";
//                    Set up the SQL connection to process the query
                    try(Connection connection = sqlConn.getConnected()){
                        System.out.println("connected to the SQL table");
                        PreparedStatement preparedStatement = connection.prepareStatement(dbRequest);
                        preparedStatement.setInt(1,studParcels.getStudentID());
//                      Execute the sql query and store your result set
                        ResultSet resultSet = preparedStatement.executeQuery();
//                        check the content of the Resultset and extract your data.
                        if(resultSet.next()){
                            noBorrowBooks = resultSet.getInt("no_of_borrowed_books");
                            checkbit = true;
                        }
                    }
//                    sending back the data to the client by updating the student object GUI
                    if (checkbit){
                        studParcels.setNoBorrowedBooks(noBorrowBooks);
                        objectOutputStream.writeObject(studParcels);
                    }
                    else{
                        studParcels.setSqlStatus("The user is not registered  in something wong");
                    }
                }
                else if (objParsing.getCommands() == clientMssg.clientCommands.BORROWEDBOOKSMENUINFO){
//                    steps
//                    1a.collect the user details
                   int ID =  studParcels.getStudentID();
//                  1b. search and collect the details of the books a student has borrowed
                    try(Connection connection = sqlConn.getConnected()){
                        System.out.println("Connected to the SQL database");
                        String SQLquery = "SELECT Borrow.ISBN, Borrow.Title, Borrow.Author FROM Borrow INNER JOIN Users ON Borrow.StudentID = Users.StudentID WHERE Borrow.StudentID = ?";
//                        1c. process the query
                        PreparedStatement preparedStatement = connection.prepareStatement(SQLquery);
                        preparedStatement.setInt(1,ID);
                        ResultSet borrowedBookList = preparedStatement.executeQuery();
//                        2a. setting up the table structure
                        List<List<Object>> tabledata = new ArrayList<>();
                        List<Object> columns = new ArrayList<>();
                        while (borrowedBookList.next()){
                            columns.add(borrowedBookList.getInt(1));
                            columns.add(borrowedBookList.getString(2));
                            columns.add(borrowedBookList.getString(3));
                            tabledata.add(columns);
                        }
                        List<String>cols = new ArrayList<>(Arrays.asList(BorroweMenuTablecols));
//                       2b. Writing back to the Client
                        if(!tabledata.isEmpty()){
                            String stats = "Contains Borrowed Books";
                            TableResponseContainer responseContainer = new TableResponseContainer(cols, tabledata,stats);
                            responseContainer.setBorrowedCode(0);
                            objectOutputStream.writeObject(responseContainer);
                        }
                        else{
                            int stats = 1;
                            TableResponseContainer responseContainer = new TableResponseContainer(stats);
                            responseContainer.setStatus("Contains no Borrowed Books");
                            objectOutputStream.writeObject(responseContainer);
                        }



                    }

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
//    public Object []setDBInitailTitleOnly()  {
//        String sqlQuery = " SELECT * FROM Books WHERE Title = ?";
//        return new Object[]{sqlQuery,connection};
//    }
//
//    public Object []setDBInitailAuthorOnly() throws SQLException {
//        String sqlQuery = " SELECT * FROM Books WHERE Author = ?";
//        Connection connection = ServerSide.sqlConn.getConnected();
//        return new Object[]{sqlQuery,connection};
//    }
//    public Object []setDBInitailATOnly() throws SQLException {
//        String sqlQuery = " SELECT * FROM Books WHERE Title = ? AND Autor = ?";
//        Connection connection = ServerSide.sqlConn.getConnected();
//        return new Object[]{sqlQuery,connection};
//    }
    public String getTitleSQL(){
        return " SELECT * FROM Books WHERE Title = ?";
    }
    public String getAuthorSQL(){
        return " SELECT * FROM Books WHERE Author = ?";
    }
    public String getbooksAllSQL(){
        return " SELECT * FROM Books WHERE Author = ? AND Title = ?";
    }
    String[] borrowedCol ={"",""};

}

//                    2. create a query to return all the books with that title and author
//                    try (Connection connection = ServerSide.sqlConn.getConnected()) {
//                        System.out.println("Connected to the DB");
//                        //Pass the title and author in the SQL command to return the lists of book(s) matching the selections
//                        PreparedStatement preparedStatement = connection.prepareStatement(returnBooksQuery);
//                        preparedStatement.setString(1, userInput[0]);
//                        preparedStatement.setString(2, userInput[1]);
//                        //store the query result in a resultset
//                        ResultSet viewBooksResult = preparedStatement.executeQuery();
//                        List<List<Object>> booksResult = new ArrayList<>();
//                        List<Object> tableCols = new ArrayList<>();
//                        while (viewBooksResult.next()) {
//                            tableCols.add(viewBooksResult.getInt(1));
//                            tableCols.add(viewBooksResult.getString(2));
//                            tableCols.add(viewBooksResult.getString(3));
//                            tableCols.add(viewBooksResult.getInt(4));
//                            booksResult.add(tableCols);
//                        }
////                        List<String> cols = null;
//                        String SQLstatusUpdate;
//                        if (!booksResult.isEmpty()) {
//                            SQLstatusUpdate = "Below are the results of books written by the author " +userInput[0];
//                            TableResponseContainer responseContainer = new TableResponseContainer(booksCols, booksResult, SQLstatusUpdate);
//                            objectOutputStream.writeObject(responseContainer);
//                            System.out.println(Arrays.toString(booksCols));
//                        } else {
//                            SQLstatusUpdate = "No such record, please try a different title and author";
//                            TableResponseContainer responseContainer = new TableResponseContainer(booksCols, booksResult, SQLstatusUpdate);
//                            responseContainer.setStatus(SQLstatusUpdate);
//                            objectOutputStream.writeObject(responseContainer);
//                            System.out.println(Arrays.toString(booksCols));
//                        }
