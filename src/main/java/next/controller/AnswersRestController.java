package next.controller;

import next.CannotDeleteException;
import next.annotation.LoginUser;
import next.dao.AnswerDao;
import next.dao.QuestionDao;
import next.model.Answer;
import next.model.Result;
import next.model.User;
import next.service.QnaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(value = "/api/answers")
public class AnswersRestController {
    private static final Logger log = LoggerFactory.getLogger(AnswersRestController.class);

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private QnaService qnaService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Answer> create(@ModelAttribute Answer answer, @LoginUser User loginUser) {
        if (loginUser.isGuestUser()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        answer.setWriter(loginUser.getUserId());
        log.debug("answer : {}", answer);

        answer = answerDao.insert(answer);
        questionDao.updateCountOfAnswer(answer.getQuestionId());

        URI location = URI.create("/" + Long.toString(answer.getAnswerId()));
        return ResponseEntity.created(location).body(answer);
    }

    @RequestMapping(value = "/{answerId}", method = RequestMethod.DELETE)
    public ResponseEntity destroy(@PathVariable long answerId, @LoginUser User loginUser) {
        if (loginUser.isGuestUser()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            qnaService.deleteAnswer(answerId, loginUser);
            return ResponseEntity.noContent().build();
        } catch (CannotDeleteException e) {
            return ResponseEntity.badRequest().body(Result.fail(e.getLocalizedMessage()));
        }
    }
}
