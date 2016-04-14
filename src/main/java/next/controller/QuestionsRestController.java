package next.controller;

import next.CannotDeleteException;
import next.annotation.LoginUser;
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
    public ResponseEntity destroy(@LoginUser User loginUser, @PathVariable long questionId) {
        if (loginUser.isGuestUser()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            qnaService.deleteQuestion(questionId, loginUser);
            return ResponseEntity.noContent().build();
        } catch (CannotDeleteException e) {
            return ResponseEntity.badRequest().body(Result.fail(e.getLocalizedMessage()));
        }
    }
}
