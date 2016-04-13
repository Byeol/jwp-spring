package next.controller;

import next.CannotDeleteException;
import next.dao.QuestionDao;
import next.model.Question;
import next.model.Result;
import next.model.User;
import next.service.QnaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping(value = "/api/questions")
public class QuestionsRestController {
    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private QnaService qnaService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Question> index() {
        return questionDao.findAll();
    }

    @RequestMapping(value = "/{questionId}", method = RequestMethod.DELETE)
    public ResponseEntity destroy(@PathVariable long questionId, HttpSession httpSession) {
        if (!UserSessionUtils.isLogined(httpSession)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = UserSessionUtils.getUserFromSession(httpSession);

        try {
            qnaService.deleteQuestion(questionId, user);
            return ResponseEntity.noContent().build();
        } catch (CannotDeleteException e) {
            return ResponseEntity.badRequest().body(Result.fail(e.getLocalizedMessage()));
        }
    }
}
