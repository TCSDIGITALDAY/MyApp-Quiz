package quiz.myapp.com.myappquiz;

/**
 * Created by venkatesh on 10/4/2017.
 */

public class Users {

    String deviceid,email,password,firstname,lastname;

    public String getDeviceid() {
        return deviceid;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
