package objParsing;

import ClientSide.Student;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * This class is responsible for sending the user inputs and commands from the client (GUI) to the server for processing.
 * The class implements serializable for  safe object stream parsing
  @author Musaddique */
public class clientMssg implements Serializable {
    //Steps
    // 1. create enum for commands from the client-side
    public enum clientCommands{
    LOGIN,CREATEUSER, CHECKFORBOOKUSERINPUT, BORROWBOOK, VIEWALLBOOKS, GETNUMBORROWBOOKS, RETURNBOOK, BORROWEDBOOKSMENUINFO
    }
    private final clientCommands commands;
    private  String statusMssg;
    private  String userInput;
    private int loginRst;
    private LocalDate returnDate;
    private List <Object> booksSelected;
    private Student studParcels;
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
     *
     * This constructor accepts two parameters, to update the server of the clients request and the user input from forms
     * @param commands the request from the GUI i.e. button clicked, or event triggered
     * @param userInput the input the user typed in the text-field provided
     */
    public clientMssg(clientCommands commands, String userInput){
        this.commands = commands;
        this.userInput = userInput;
    }
    public clientMssg (clientCommands commands, Student studParcels, String userInput){
        this.commands =commands;
        this.studParcels =studParcels;
        this.userInput =userInput;
    }
/**This constructor takes in the command and a list of objects**/
    public clientMssg(clientCommands commands, Student studParcels, List<Object> booksSelected){
        this.commands = commands;
        this.booksSelected = booksSelected;
        this.studParcels = studParcels;
    }

//    public clientMssg(clientCommands commands, List <Object>  selectedBooks, LocalDate borrowDate){
//        this.commands = commands;
//        this.booksSelected = selectedBooks;
//        this.returnDate = borrowDate;
//    }

    public clientMssg (clientCommands commands, Student studParcels){
        this.commands =commands;
        this.studParcels =studParcels;
    }

    public clientMssg(clientCommands commands){
        this.commands = commands;
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

    public LocalDate getReturnDate(){
        return returnDate;
    }

    public String getStatusMssg() {
        return statusMssg;
    }

    public  List <Object>  getBooksSelected() {
        return booksSelected;
    }

    public void setBooksSelected(List<Object> booksSelected) {
        this.booksSelected = booksSelected;
    }

    public Student getStudParcels() {
        return studParcels;
    }

    public void setStudParcels(Student studParcels) {
        this.studParcels=studParcels;
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
