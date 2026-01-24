abstract class staffUser {
    String userId;
    String usename;
    String role;
    String password;
    int authId;

    abstract void showDashboard();
    abstract void logout();

}
