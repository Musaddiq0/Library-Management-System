package ClientSide;

import java.io.Serializable;

public class Student implements Serializable {
    public String firstName;
    private String lastName;
    private  int studentID;
    private String password ;
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


    public Student (int studentID, String password){
        this.studentID =studentID;
        this.password =password;
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

    public String getPassword() {
        return password;
    }

    public void setNoBorrowedBooks(int noBorrowedBooks) {
        this.noBorrowedBooks = noBorrowedBooks;
    }

    public void setSqlStatus(String sqlStatus) {
        this.sqlStatus = sqlStatus;
    }
    public void setPassword(String password){this.password =password;}
    public  void setFirstName(String firstName){
        this.firstName=firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void resetStudent(Student fakeStudent) {
        fakeStudent.studentID =0;
        fakeStudent.setPassword(null);
        fakeStudent.setFirstName(null);
    }
}
