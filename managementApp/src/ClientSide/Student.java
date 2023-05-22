package ClientSide;

import java.io.Serializable;

public class Student implements Serializable {
    private String firstName;
    private String lastName;
    private int studentID;
    private  int noBorrowedBooks;
    private String status;


    public Student(int studentID,String firstName,String lastName){
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentID =studentID;
    }

    public String getFirstName() {
        return firstName;
    }

    public int getNoBorrowedBooks() {
        return noBorrowedBooks;
    }

    public String getLastName() {
        return lastName;
    }

    public String getStatus() {
        return status;
    }

    public int getStudentID() {
        return studentID;
    }

    public void setNoBorrowedBooks(int noBorrowedBooks) {
        this.noBorrowedBooks = noBorrowedBooks;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
