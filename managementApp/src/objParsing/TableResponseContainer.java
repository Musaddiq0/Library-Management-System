package objParsing;

import java.io.Serializable;
import java.util.List;
/**
 * This class purpose in life is to encapsulate sql query result data (columns and rows) to display on the JTable accordingly
 * @author Musaddique
 * */
public class TableResponseContainer implements Serializable {
    public List<String> columns;
    public List<List<Object>> data;
    public String status;
    public int borrowedCode;
//    public List<String> booksCols;

    /**
     * this contructor sets the columns names for the table and the stores the rows of result of the query as list of objects
     *
     * @param columns      takes in the column count and the names of the columns  for the table
     * @param data         this takes in the rows gotten from the database as objects
     * @param status       this returns the status of the result from the server
     */

//    public TableResponseContainer(String[] columns, List<List<Object>> data, String noResultMssg) {
//        this.columns = columns;
//        this.data = data;
//    }

    public TableResponseContainer(List<String> columns, List<List<Object>> data, String status) {
        this.columns = columns;
        this.data = data;
        this.status = status;

    }
    public TableResponseContainer (int borrowedCode){
        this.borrowedCode = borrowedCode;
    }

//    public TableResponseContainer(List<String> booksCols) {
//     this.booksCols = booksCols;
//    }

    public String getStatus() {
        return status;
    }
    public int getBorrowedCode(){
        return borrowedCode;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBorrowedCode(int borrowedCode) {
        this.borrowedCode = borrowedCode;
    }
}
//    public void setBooksCols(int index, String colname) {
//        this.booksCols.set(index, colname);
//    }
//    public List<String> getBooksCols() {
//        return booksCols;
//    }
//}

