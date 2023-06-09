package ClientSide;

import java.io.Serializable;

public class Student implements Serializable {
    private final String firstName;
    private final String lastName;
    private final int studentID;
    private  int noBorrowedBooks;
    private String sqlStatus;

/**This class sole purpose is to get students data from the database table it is  also used to transport information between server and client
 * @param studentID student ID as it is on the db
 * @param firstName student firstname as it is on the db
 * @param lastName  student lastname as it is on the db
 * **/
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

    public String getSqlStatus() {
        return sqlStatus;
    }

    public int getStudentID() {
        return studentID;
    }

    public void setNoBorrowedBooks(int noBorrowedBooks) {
        this.noBorrowedBooks = noBorrowedBooks;
    }

    public void setSqlStatus(String sqlStatus) {
        this.sqlStatus = sqlStatus;
    }

}
