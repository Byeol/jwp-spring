package next.controller;

import next.CannotDeleteException;
import next.annotation.LoginUser;
import next.dao.AnswerDao;
import next.dao.QuestionDao;
import next.model.Answer;
import next.model.Question;
import next.model.User;
import next.service.QnaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/questions")
public class QuestionsController {
    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private QnaService qnaService;

    @RequestMapping(method = RequestMethod.GET)
    public String index() {
        return "redirect:/";
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String form(@LoginUser User loginUser) {
        if (loginUser.isGuestUser()) {
            return "redirect:/users/login";
        }
        return "qna/form";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String create(@LoginUser User loginUser, @ModelAttribute Question question) {
        if (loginUser.isGuestUser()) {
            return "redirect:/users/login";
        }

        question.setWriter(loginUser.getUserId());
        questionDao.insert(question);
        return "redirect:/";
    }

    @RequestMapping(value = "/{questionId}", method = RequestMethod.GET)
    public String show(@PathVariable long questionId, Model model) {
        Question question = questionDao.findById(questionId);
        List<Answer> answers = answerDao.findAllByQuestionId(questionId);

        model.addAttribute("question", question);
        model.addAttribute("answers", answers);
        return "qna/show";
    }

    @RequestMapping(value = "/{questionId}/edit", method = RequestMethod.GET)
    public String edit(@PathVariable long questionId, @LoginUser User loginUser, Model model) {
        if (loginUser.isGuestUser()) {
            return "redirect:/users/login";
        }

        Question question = questionDao.findById(questionId);
        if (!question.isSameUser(loginUser)) {
            throw new IllegalStateException("다른 사용자가 쓴 글을 수정할 수 없습니다.");
        }

        model.addAttribute("question", question);
        return "qna/update";
    }

    @RequestMapping(value = "/{questionId}", method = RequestMethod.PUT)
    public String update(@PathVariable long questionId, @LoginUser User loginUser, @ModelAttribute Question newQuestion) {
        if (loginUser.isGuestUser()) {
            return "redirect:/users/login";
        }

        Question question = questionDao.findById(questionId);
        if (!question.isSameUser(loginUser)) {
            throw new IllegalStateException("다른 사용자가 쓴 글을 수정할 수 없습니다.");
        }

        question.update(newQuestion);
        questionDao.update(question);
        return "redirect:/";
    }

    @RequestMapping(value = "/{questionId}", method = RequestMethod.DELETE)
    public String destroy(@PathVariable long questionId, @LoginUser User loginUser, RedirectAttributes redirectAttrs) {
        if (loginUser.isGuestUser()) {
            return "redirect:/users/login";
        }

        try {
            qnaService.deleteQuestion(questionId, loginUser);
            return "redirect:/";
        } catch (CannotDeleteException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getLocalizedMessage());
            return "redirect:/questions/" + questionId;
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(IllegalStateException.class)
    public RedirectView forbidden(HttpServletRequest request, Exception exception) throws SQLException {
        RedirectView rw = new RedirectView(".");
        rw.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
        FlashMap outputFlashMap = RequestContextUtils.getOutputFlashMap(request);
        outputFlashMap.put("errorMessage", exception.getLocalizedMessage());
        return rw;
    }
}
