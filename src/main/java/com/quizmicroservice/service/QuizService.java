package com.quizmicroservice.service;

import com.quizmicroservice.dto.QuestionWrapper;
import com.quizmicroservice.dto.UserResponse;
import com.quizmicroservice.model.Question;
import com.quizmicroservice.model.Quiz;
import com.quizmicroservice.model.Result;
import com.quizmicroservice.repository.QuestionRepository;
import com.quizmicroservice.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ResultService resultService; // <-- inject ResultService

    public QuizService(QuizRepository quizRepository,
                       QuestionRepository questionRepository,
                       ResultService resultService) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.resultService = resultService;
    }

    public void createQuiz(String category, int numQ, String title) {
        List<Question> questions = questionRepository.findRandomQuestionsByCategory(category, numQ);

        if (questions.isEmpty()) {
            throw new IllegalArgumentException("No questions found for category: " + category);
        }

        if (questions.size() < numQ) {
            throw new IllegalArgumentException(
                    "Only " + questions.size() + " questions are available for category: " + category
            );
        }

        Quiz quiz = new Quiz();
        quiz.setTitle(title.trim());
        quiz.setQuestions(questions);
        quizRepository.save(quiz);
    }

    public List<QuestionWrapper> getQuizQuestions(Integer id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with id: " + id));

        List<QuestionWrapper> questionsForUser = new ArrayList<>();

        for (Question q : quiz.getQuestions()) {
            questionsForUser.add(new QuestionWrapper(
                    q.getId(),
                    q.getTitle(),
                    q.getOption1(),
                    q.getOption2(),
                    q.getOption3(),
                    q.getOption4()
            ));
        }

        return questionsForUser;
    }

    public int calculateScore(Integer id, List<UserResponse> responses) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with id: " + id));

        Map<Integer, String> correctAnswers = new HashMap<>();

        for (Question question : quiz.getQuestions()) {
            correctAnswers.put(question.getId(), question.getRightAnswer());
        }

        int score = 0;

        for (UserResponse response : responses) {
            if (response.getId() == null || response.getResponse() == null) {
                continue;
            }

            String correctAnswer = correctAnswers.get(response.getId());

            if (correctAnswer != null &&
                    response.getResponse().trim().equalsIgnoreCase(correctAnswer.trim())) {
                score++;
            }
        }

        int total = quiz.getQuestions().size();
        double percentage = total > 0 ? (score * 100.0) / total : 0.0;

        // TODO: replace with real logged-in user data when you pass it in
        String studentName = "Unknown Student";
        String email = "unknown@example.com";

        Result result = new Result(
                studentName,
                email,
                quiz.getTitle(),
                score,
                total,
                percentage,
                LocalDateTime.now()
        );

        // save result so admin can see it later
        resultService.saveResult(result);

        return score;
    }
}