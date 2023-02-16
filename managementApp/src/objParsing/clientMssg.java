package objParsing;

import java.io.Serializable;
import java.util.List;

/**
 * This class is responsible for sending the user inputs and commands from the client (GUI) to the server for processing.
 * The class implements serializable for  safe object stream parsing
  @author Musaddique */
public class clientMssg implements Serializable {
    //Steps
    // 1. create enum for commands from the client-side
    public enum clientCommands{
    LOGIN, VIEWBOOKSUSINGAT, BORROWBOOK
    }
//    public  enum booksCols{
//        ISBN,Title,Author,Quantity
//    }
    private final clientCommands commands;
    private  String statusMssg;
    private  String userInput;
    private int loginRst;
    private Object booksSelected;
//    private final bo


    // 2. constructor for this class
    /**
     * This constructs accepts one parameter, to update the server of the clients command when no user input is considered
     * i.e. like a click of the button
     * @param commands the command from the GUI i.e. button clicked (
     */
    public clientMssg(clientCommands commands, Object books) {
        this.commands = commands;
        this.booksSelected = books;
    }
    /**
     * This constructor accepts two parameters, to update the server of the clients command and the user input
     * @param commands the command from the GUI i.e. button clicked
     * @param userInput the input the user typed in the text-field provided
     */
    public clientMssg(clientCommands commands, String userInput){
        this.commands = commands;
        this.userInput = userInput;
    }

    public clientMssg(clientCommands commands, int loginRst){
        this.commands = commands;
        this.loginRst =loginRst;
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

    public String getStatusMssg() {
        return statusMssg;
    }

    public Object getBooksSelected() {
        return booksSelected;
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
