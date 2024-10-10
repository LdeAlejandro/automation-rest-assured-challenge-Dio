package Entities;

public class TokenUser {

    private String username = "admin";
    private String password = "password123";

    public TokenUser() {

    }

    public TokenUser(String userName,String passWord) {
        this.username = userName;
        this.password = passWord;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
