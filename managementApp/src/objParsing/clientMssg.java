package objParsing;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * This class is responsible for sending the user inputs and commands from the client (GUI) to the server for processing.
 * The class implements serializable for  safe object stream parsing
  @author Musaddique */
public class clientMssg implements Serializable {
    //Steps
    // 1. create enum for commands from the client-side
    public enum clientCommands{
    LOGIN, VIEWBOOKSUSINGAT, BORROWBOOK, UserInfoParcel,
    }
//    public  enum booksCols{
//        ISBN,Title,Author,Quantity
//    }
    private final clientCommands commands;
    private  String statusMssg;
    private  String userInput;
    private int loginRst;
    private Date returnDate;
    private List <Object> booksSelected;
    private Object studParcels;
//    private final bo


    // 2. constructor for this class
//    /**
//     * This constructs accepts one parameter, to update the server of the clients command when no user input is considered
//     * i.e. like a click of the button
//     * @param commands the command from the GUI i.e. button clicked (
//     */
////    public clientMssg(clientCommands commands, List <Object> books) {
////        this.commands = commands;
////        this.booksSelected = books;
////    }
    /**
     * This constructor accepts two parameters, to update the server of the clients command and the user input
     * @param commands the command from the GUI i.e. button clicked
     * @param userInput the input the user typed in the text-field provided
     */
    public clientMssg(clientCommands commands, String userInput){
        this.commands = commands;
        this.userInput = userInput;
    }

//    public clientMssg(clientCommands commands, Object booksInfo, String date){
//        this.commands = commands;
//        this.booksSelected =booksInfo;
//        this.userInput = date;
//    }

    public clientMssg(clientCommands commands, List <Object>  selectedBooks, Date borrowDate){
        this.commands = commands;
        this.booksSelected = selectedBooks;
        this.returnDate = borrowDate;

    }

    public clientMssg (clientCommands commands, Object studParcels){
        this.commands =commands;
        this.studParcels =studParcels;
    }

    // 3. setters and getters to update the server and clients about commands


    public clientCommands getCommands() {
        return commands;
    }
//    public booksCols getcommands(){
//        return booksCols;
//    }

    public String getUserInput() {
        return userInput;
    }

    public Date getReturnDate(){
        return returnDate;
    }

    public String getStatusMssg() {
        return statusMssg;
    }

    public  List <Object>  getBooksSelected() {
        return booksSelected;
    }

    public Object getStudParcels() {
        return studParcels;
    }

    public int getLoginStatus() {
        return loginRst;
    }
    public void setStatusMssg(String statusMssg) {
        this.statusMssg = statusMssg;
    }
    public void setLoginStatus(int sqlRst){
        this.loginRst = sqlRst;
    }

}
