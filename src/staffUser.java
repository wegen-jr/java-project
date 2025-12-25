abstract class staffUser {
    String userId;
    String usename;
    String role;
    String password;

    abstract void showDashboard();
    abstract Boolean login(String String);
    abstract void logout();

}
