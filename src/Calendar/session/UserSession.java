package Calendar.session;

import Calendar.model.User;

public class UserSession {

    private static User currentUser;

    private UserSession() {
    }

    // 로그인 성공 후 앱 전역에서 사용할 현재 사용자 정보를 보관한다.
    public static void login(User user) {
        if (user == null) {
            throw new IllegalArgumentException("세션에 저장할 사용자가 없습니다.");
        }
        currentUser = user;
    }

    // UI에서 현재 로그인한 사용자 정보가 필요할 때 사용한다.
    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // 로그아웃하거나 앱을 초기 상태로 되돌릴 때 세션을 비운다.
    public static void logout() {
        currentUser = null;
    }
}
