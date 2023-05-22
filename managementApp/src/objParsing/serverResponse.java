package objParsing;

import java.io.Serializable;
import java.util.List;

public class serverResponse implements Serializable {
    private int borrowFlag ;
    private String mssgToDisplay;
    private List<Object> userInfoParcels;
    private String sqlcode;

    public serverResponse(int sqlStat, String labelMssg){
        this.borrowFlag =sqlStat;
        this.mssgToDisplay =labelMssg;
    }

    public serverResponse(){

    }
/**This constructor handles the userinfo panels info
 * @param userInfo this contains the array of string containing the student info (StudentID,Firstname,Lastname)
 **/
    public serverResponse(List<Object> userInfo){
        this.userInfoParcels=userInfo;
//        this.sqlcode =sqlStatCode;

    }


    public int getBorrowFlag() {
        return borrowFlag;
    }

    public List<Object> getUserInfoParcels() {
        return userInfoParcels;
    }

    public String getMssgToDisplay() {
        return mssgToDisplay;
    }

    public String getSqlcode() {
        return sqlcode;
    }

    public void setMssgToDisplay(String mssgToDisplay) {
        this.mssgToDisplay = mssgToDisplay;
    }

    public void setBorrowFlag(int borrowFlag) {
        this.borrowFlag = borrowFlag;
    }

    public void setUserInfoParcels(List<Object> userInfoParcels) {
        this.userInfoParcels = userInfoParcels;
    }

    public void setSqlcode(String sqlcode) {
        this.sqlcode = sqlcode;
    }
}
