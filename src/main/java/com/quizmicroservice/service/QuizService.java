package com.quizmicroservice.service;

import com.quizmicroservice.dto.QuestionWrapper;
import com.quizmicroservice.dto.UserResponse;
import com.quizmicroservice.model.Question;
import com.quizmicroservice.model.Quiz;
import com.quizmicroservice.model.Result;
import com.quizmicroservice.model.User;
import com.quizmicroservice.repository.QuizRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final ResultService resultService;
    private final UserService userService;

    public QuizService(QuizRepository quizRepository,
                       ResultService resultService,
                       UserService userService) {
        this.quizRepository = quizRepository;
        this.resultService = resultService;
        this.userService = userService;
    }

    public Integer createQuiz(String subject, String difficulty) {
        String subj = subject != null ? subject.trim() : null;
        String diff = difficulty != null ? difficulty.trim().toUpperCase() : null;

        if (subj == null || subj.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject is required.");
        }

        if (diff == null || diff.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Difficulty is required.");
        }

        Quiz quiz = quizRepository.findBySubjectAndDifficulty(subj, diff)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Quiz not found for subject: " + subj + " and difficulty: " + diff
                ));

        return quiz.getId();
    }

    @Transactional(readOnly = true)
    public List<QuestionWrapper> getQuizQuestions(Integer id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Quiz not found with id: " + id
                ));

        return quiz.getQuestions()
                .stream()
                .map(q -> new QuestionWrapper(
                        q.getId(),
                        q.getQuestion(),
                        q.getOption1(),
                        q.getOption2(),
                        q.getOption3(),
                        q.getOption4()
                ))
                .toList();
    }
    
    @Transactional
    public int calculateScore(
            Integer id,
            List<UserResponse> responses,
            String timeTaken
    ) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Quiz not found with id: " + id
                ));

        Map<Integer, Integer> correctOptions = new HashMap<>();
        for (Question question : quiz.getQuestions()) {
            correctOptions.put(question.getId(), question.getCorrectOption());
        }

        int score = 0;
        int attemptedCount = 0;

        if (responses != null) {
            for (UserResponse response : responses) {
                if (response == null || response.getId() == null || response.getSelectedOption() == null) {
                    continue;
                }

                attemptedCount++;

                Integer correctOpt = correctOptions.get(response.getId());
                if (correctOpt != null && correctOpt.equals(response.getSelectedOption())) {
                    score++;
                }
            }
        }

        int total = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
        double percentage = total > 0 ? (score * 100.0) / total : 0.0;

        String studentName = "Unknown Student";
        String email = "unknown@example.com";
        User user = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            user = userService.findByEmail(username);
            if (user != null) {
                studentName = ((user.getFirstName() != null ? user.getFirstName() : "") +
                        (user.getLastName() != null && !user.getLastName().isBlank() ? " " + user.getLastName() : ""))
                        .trim();

                if (studentName.isBlank()) {
                    studentName = user.getUsername();
                }

                email = user.getEmail();
            }
        }

        String subject = quiz.getSubject();
        String difficulty = quiz.getDifficulty();
        String quizTitle = subject + " - " + difficulty;

        Result result = new Result(
        	    studentName,
        	    email,
        	    subject,
        	    difficulty,
        	    quizTitle,
        	    score,
        	    total,
        	    percentage,
        	    timeTaken,
        	    attemptedCount,
        	    LocalDateTime.now()
        	);

        	result.setUser(user);
        	result.setQuiz(quiz);

        	resultService.saveResult(result);

        return score;
    }
}