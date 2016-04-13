package next.controller;

import next.BadRequestException;
import next.dao.UserDao;
import next.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.sql.SQLException;

@Controller
@RequestMapping("/users")
public class UsersController {
    private static final Logger log = LoggerFactory.getLogger(UsersController.class);

    @Autowired
    private UserDao userDao;

    @RequestMapping(method = RequestMethod.GET)
    public String index(HttpSession httpSession, Model model) throws SQLException {
        if (!UserSessionUtils.isLogined(httpSession)) {
            return "redirect:/users/login";
        }

        model.addAttribute("users", userDao.findAll());
        return "user/list";
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String form() {
        return "user/form";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String create(@ModelAttribute @Valid User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException();
        }

        log.debug("User: {}", user);
        userDao.insert(user);
        return "redirect:/";
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public String show(@PathVariable String userId, Model model) {
        User user = userDao.findByUserId(userId);
        model.addAttribute("user", user);
        return "user/profile";
    }

    @RequestMapping(value = "/{userId}/edit", method = RequestMethod.GET)
    public String edit(@PathVariable String userId, HttpSession httpSession, Model model) {
        User user = userDao.findByUserId(userId);

        if (!UserSessionUtils.isSameUser(httpSession, user)) {
            throw new IllegalStateException("다른 사용자의 정보를 수정할 수 없습니다.");
        }

        model.addAttribute("user", user);
        return "user/updateForm";
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT)
    public String update(@PathVariable String userId, @ModelAttribute User updateUser, HttpSession httpSession) {
        User user = userDao.findByUserId(userId);

        if (!UserSessionUtils.isSameUser(httpSession, user)) {
            throw new IllegalStateException("다른 사용자의 정보를 수정할 수 없습니다.");
        }

        log.debug("Update User : {}", updateUser);
        user.update(updateUser);
        userDao.update(user);
        return "redirect:/";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public String badRequest(Model model) {
        model.addAttribute("error", true);
        return form();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(IllegalStateException.class)
    public String forbidden(HttpSession httpSession, Model model, Exception exception) throws SQLException {
        model.addAttribute("errorMessage", exception.getMessage());
        return index(httpSession, model);
    }
}
