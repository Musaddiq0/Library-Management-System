package objParsing;

import java.io.Serializable;

public class serverResponse implements Serializable {
    private int borrowFlag ;
    private String mssgToDisplay;

    public serverResponse(int sqlStat, String labelMssg){
        this.borrowFlag =sqlStat;
        this.mssgToDisplay =labelMssg;
    }

    public serverResponse(){
    }

    public int getBorrowFlag() {
        return borrowFlag;
    }

    public String getMssgToDisplay() {
        return mssgToDisplay;
    }

    public void setMssgToDisplay(String mssgToDisplay) {
        this.mssgToDisplay = mssgToDisplay;
    }

    public void setBorrowFlag(int borrowFlag) {
        this.borrowFlag = borrowFlag;
    }
}
