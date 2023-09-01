package objParsing;

import java.io.Serializable;
import java.util.List;

public class serverResponse implements Serializable {
    private int serverResponseFlag;
    private String mssgToDisplay;
    private List<Object> userInfoParcels;
    private String sqlcode;
    private String reutrnMessage;
    private int returnCode;

    public serverResponse(int serverResponseCode, String labelMssg){
        this.serverResponseFlag =serverResponseCode;
        this.mssgToDisplay =labelMssg;
    }

    public serverResponse(){

    }
///**This constructor handles the userinfo panels info
// * @param userInfo this contains the array of string containing the student info (StudentID,Firstname,Lastname)
// **/
//    public serverResponse(List<Object> userInfo){
//        this.userInfoParcels=userInfo;
////        this.sqlcode =sqlStatCode;
//
//    }


    public int getServerResponseFlag() {
        return serverResponseFlag;
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

    public void setServerResponseFlag(int serverResponseFlag) {
        this.serverResponseFlag = serverResponseFlag;
    }

    public void setUserInfoParcels(List<Object> userInfoParcels) {
        this.userInfoParcels = userInfoParcels;
    }

    public void setSqlcode(String sqlcode) {
        this.sqlcode = sqlcode;
    }
}
