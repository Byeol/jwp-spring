package next.controller;

import next.dao.UserDao;
import next.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("/users")
public class LoginController {
    public static final String USER_SESSION_KEY = "user";

    @Autowired
    private UserDao userDao;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginForm() {
        return "user/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestParam String userId, @RequestParam String password, NativeWebRequest webRequest) {
        User user = userDao.findByUserId(userId);

        if (user == null) {
            throw new NullPointerException("사용자를 찾을 수 없습니다.");
        }

        if (user.matchPassword(password)) {
            webRequest.setAttribute(USER_SESSION_KEY, user, WebRequest.SCOPE_SESSION);
            return "redirect:/";
        } else {
            throw new IllegalStateException("비밀번호가 틀립니다.");
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(NativeWebRequest webRequest) {
        webRequest.removeAttribute(USER_SESSION_KEY, WebRequest.SCOPE_SESSION);
        return "redirect:/";
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({ NullPointerException.class, IllegalStateException.class })
    public String loginFailed(Model model) {
        model.addAttribute("loginFailed", true);
        return loginForm();
    }
}
