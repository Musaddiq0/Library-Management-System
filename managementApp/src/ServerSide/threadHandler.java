package ServerSide;

import ClientSide.Student;
import objParsing.TableResponseContainer;
import objParsing.clientMssg;
import objParsing.serverResponse;
import org.jetbrains.annotations.NotNull;

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
    String[] booksCols = {"BookID", "Title", "Author", "Genre","Quantity"};
    String[] BorroweMenuTablecols = {"BorrowID", "Title", "Author"};

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
                    String[]userInput = clientSays.split("\\$");
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
                            while (viewBooksResult.next()) {
                                List<Object> tableCols = new ArrayList<>();
                                tableCols.add(viewBooksResult.getInt(1)); //bookid
                                tableCols.add(viewBooksResult.getString(2));//title
                                tableCols.add(viewBooksResult.getString(3));//author
                                tableCols.add(viewBooksResult.getString(4));//genre
                                tableCols.add(viewBooksResult.getInt(5));//quantity
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
                            while (viewBooksResult.next()) {
                                List<Object> tableCols = new ArrayList<>();
                                tableCols.add(viewBooksResult.getInt(1)); //bookid
                                tableCols.add(viewBooksResult.getString(2));//title
                                tableCols.add(viewBooksResult.getString(3));//author
                                tableCols.add(viewBooksResult.getString(4));//genre
                                tableCols.add(viewBooksResult.getInt(5));//quantity
                                booksResult.add(tableCols);
                            }
//                            sending back to the server to display
                            List<String> cols = new ArrayList<>(Arrays.asList(booksCols));
                            String SQLstatusUpdate;
                            if (!booksResult.isEmpty()){
                                SQLstatusUpdate = "Below are the results of books marching the title " +userInput[1];
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
                                tableCols.add(viewBooksResult.getInt(1)); //bookid
                                tableCols.add(viewBooksResult.getString(2));//title
                                tableCols.add(viewBooksResult.getString(3));//author
                                tableCols.add(viewBooksResult.getString(4));//genre
                                tableCols.add(viewBooksResult.getInt(5));//quantity
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
                    String sortedInput = getString(borrowBooksInfo);
//                    split the sorted client details and store in an array
                    String[] userInput = sortedInput.split("\\&");
                    Date date = objParsing.getReturnDate();
//                    convert the date to SQL date format
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
//                  set the SQL query
                    String sql = "INSERT INTO Borrow (BorrowID, Title, Author, ReturnDate,StudentID,BookID,Genre) VALUES (?, ?, ?, ?, ?, ?,?)";
//                    Create the SQL connection
                    try(Connection connect = sqlConn.getConnected()){
                        System.out.println("connection to DB established");
//                        update the borrow table on the SQL to reflect the user request
                        PreparedStatement preparedStatement = connect.prepareStatement(sql);
                        preparedStatement.setInt(1, Integer.parseInt(userInput[4].trim())); //BorrowID
                        preparedStatement.setString(2,userInput[2].trim());  //Title
                        preparedStatement.setString(3, userInput[3].trim()); //Author
                        preparedStatement.setDate(4, (sqlDate)); //ReturnDate
                        preparedStatement.setInt(5, Integer.parseInt(userInput[0].trim())); //StudentID,
                        preparedStatement.setInt(6, Integer.parseInt(userInput[1].trim())); //BorrowID
                        preparedStatement.setString(7,userInput[5].trim());//genre
//                        store the query result code
                        int sqlStatus = preparedStatement.executeUpdate();
                        String dbResponse = null;
                        serverResponse response = new serverResponse();
                        //2. update the GUI with the results from the action performed with the student details and the book borrowed
                        if (sqlStatus >0){
                            dbResponse = "Please proceed to the counter to collect your book. Enjoy have a great ";
                            response.setServerResponseFlag(sqlStatus);
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
                            response.setServerResponseFlag(sqlStatus);
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
                        studParcels.setSqlStatus("The user is not registered  please register firt to gain access");
                    }
                }
                else if (objParsing.getCommands() == clientMssg.clientCommands.BORROWEDBOOKSMENUINFO){
//                    steps
//                    1a.collect the user details
                   int ID =  studParcels.getStudentID();
//                  1b. search and collect the details of the books a student has borrowed
                    try(Connection connection = sqlConn.getConnected()){
                        System.out.println("Connected to the SQL database");
                        String SQLquery = "SELECT Borrow.BorrowID, Borrow.Title, Borrow.Author FROM Borrow INNER JOIN Users ON Borrow.StudentID = Users.StudentID WHERE Borrow.StudentID = ?";
//                        1c. process the query
                        PreparedStatement preparedStatement = connection.prepareStatement(SQLquery);
                        preparedStatement.setInt(1,ID);
                        ResultSet borrowedBookList = preparedStatement.executeQuery();
//                        2a. setting up the table structure
                        List<List<Object>> tabledata = new ArrayList<>();
                        while (borrowedBookList.next()){
//                            store the data before
                            List<Object> columns = new ArrayList<>();
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
                else if (objParsing.getCommands() == clientMssg.clientCommands.RETURNBOOK) {
//                    steps
//                    1. Gather user input to process the selected entry to return
                    String sortedInput = getString(borrowBooksInfo);
//                    split the sorted client details and store in an array
                    String[] userInput = sortedInput.split("\\&");
//                    2. update the books and borrow table respectively
//                    a. checking the input to validate it
                    String returnProcess1 = "Select * FROM Borrow WHERE BorrowID =?";
                    String returnProcess2 = "DELETE FROM Borrow WHERE BorrowID =? ";
                    String returnProcess3 = "Select Quantity FROM Books WHERE BookID =?";
                    String finalReturnProcess = "UPDATE Books SET Quantity =? WHERE BookID=?";
                    int sqlCode = 0;
                    String responseTxt = new String();
//                    b. make the SQL connection to the table
                    try(Connection connection = sqlConn.getConnected()){
//                        pre-run the query to get determine the result
                        PreparedStatement preparedStatement = connection.prepareStatement(returnProcess1);
                        preparedStatement.setInt(1, (Integer) borrowBooksInfo.get(0));
                        ResultSet resultSet1 = preparedStatement.executeQuery();
                        List<Object>returnP1Data = new ArrayList<>();
                        while (resultSet1.next()){
                            returnP1Data.add(resultSet1.getInt(1));//borrowID
                            returnP1Data.add(resultSet1.getString(2));//title
                            returnP1Data.add(resultSet1.getString(3));//author
                            returnP1Data.add(resultSet1.getInt(5));//studentID
                            returnP1Data.add(resultSet1.getInt(6));//bookID
                        }
                        if(returnP1Data.isEmpty()){
//                            List<Object> borrowID = tablesResult.get(0);
                            System.out.println("Something wrong user reach here!!!!");
                        }
                        else{
//                            delete the borrow entry for that user  on the DB
                            preparedStatement = connection.prepareStatement(returnProcess2);
                            preparedStatement.setInt(1, (Integer) returnP1Data.get(0));
                            sqlCode = preparedStatement.executeUpdate();
//                            update books table after deletion from borrow table
                            if(sqlCode>0){
//                                retrieve the quantity from the table to update it with the return book by the user
                                preparedStatement = connection.prepareStatement(returnProcess3);
                                preparedStatement.setInt(1, (Integer) returnP1Data.get(5));
                                ResultSet resultSet2 = preparedStatement.executeQuery();
                                List<Object>returnP2data = new ArrayList<>();
                                while(resultSet2.next()){
                                    returnP2data.add(resultSet2.getInt(1));//book quantity
                                }
                                int newQuantity  = (Integer)returnP2data.get(0) + 1;
//                                final return process update the books table so that the quantity value goes up with one
                                if(newQuantity<0){
                                    preparedStatement = connection.prepareStatement(finalReturnProcess);
                                    preparedStatement.setInt(1,newQuantity);
                                    preparedStatement.setInt(2, (Integer) returnP1Data.get(5));
                                    int rowsAffected = preparedStatement.executeUpdate();
                                    if (rowsAffected>0){
                                        responseTxt = "Sucessfully returned the book thank you have a nice day !!!";
                                    }
                                }
                            }
                        }
//                        Writting back to the client GUI
                        serverResponse response = new serverResponse(sqlCode,responseTxt);
                        response.setServerResponseFlag(sqlCode);
                        response.setMssgToDisplay(responseTxt);
                        objectOutputStream.writeObject(response);
                    }



                }
            }
        }catch (IOException ex){
            Logger.getLogger(threadHandler.class.getName()).log(Level.SEVERE,null,ex);
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }finally {
//            killing the thread after completing a client - server communication
            try{
                threadCount("We have lost connection to the client " + threadNo + ".");
                socket.close();
            }catch (IOException ex){
                Logger.getLogger(threadHandler.class.getName()).log(Level.SEVERE,null,ex);
            }
        }


    }


    @NotNull
    private static String getString(List<Object> borrowBooksInfo) {
        StringBuilder builder = new StringBuilder();
// concatenating the user input to one string to send to the server
        for (Object str : borrowBooksInfo) {
            builder.append(String.format("%s&", str));
        }
        String sortedInput = builder.toString();
        if (!sortedInput.isEmpty()) {
            sortedInput = sortedInput.substring(0, sortedInput.length() - 1); // Remove the trailing colon
        }
        return sortedInput;
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

////    public List<List<Object>> retrnAllColumns (ResultSet resultSet, int numberOfCols) throws SQLException {
////        List<List<Object>>tableData = new ArrayList<>();
////        while (resultSet.next()){
////            List<Object>cols = new ArrayList<>();
////            cols.add(resultSet)
////
////
////        }
////        collect and store the data for the umb
//
//        return tableData;
//    }



//    public List<Object> GetSQlResults (String query, List<Object> userInput) {
//        List<Object> results = new ArrayList<>();
//        try (Connection connection = sqlConn.getConnected()) {
////           pre-run the query to get determine the result
//            PreparedStatement preparedStatement = connection.prepareStatement(query);
//
//            preparedStatement.setInt(1, (Integer) userInput.get(0));
//            ResultSet resultSet1 = preparedStatement.executeQuery();
//            List<List<Object>> tablesResult = new ArrayList<>();
//            while (resultSet1.next()) {
//                List<Object> columnsData = new ArrayList<>();
//                columnsData.add(resultSet1.getInt(1));
//                tablesResult.add(columnsData);
//            }
//
//            return results;
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
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
