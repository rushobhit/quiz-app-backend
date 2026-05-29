package com.quizmicroservice.service;

import com.quizmicroservice.dto.QuestionRequest;
import com.quizmicroservice.model.Question;
import com.quizmicroservice.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    // ---------- student / common methods ----------

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public List<Question> getQuestionsByCategory(String category) {
        return questionRepository.findByCategory(category);
    }

    public void addQuestion(QuestionRequest request) {
        if (!request.hasValidCorrectAnswer()) {
            throw new IllegalArgumentException(
                    "Correct answer must match one of the provided options."
            );
        }

        List<String> options = request.getOptions()
                .stream()
                .map(String::trim)
                .toList();

        if (options.size() != 4) {
            throw new IllegalArgumentException("Exactly 4 options are required.");
        }

        Question question = new Question();
        question.setTitle(request.getQuestion().trim());
        question.setCategory(request.getCategory().trim());
        question.setRightAnswer(request.getCorrectAnswer().trim());
        question.setOption1(options.get(0));
        question.setOption2(options.get(1));
        question.setOption3(options.get(2));
        question.setOption4(options.get(3));

        questionRepository.save(question);
    }

    public void deleteQuestion(int id) {
        if (!questionRepository.existsById(id)) {
            throw new IllegalArgumentException("Question not found with id: " + id);
        }

        questionRepository.deleteById(id);
    }

    // ---------- admin methods ----------

    /**
     * Returns all questions as QuestionRequest DTOs for admin dashboard.
     */
    public List<QuestionRequest> getAllForAdmin() {
        return questionRepository.findAll()
                .stream()
                .map(this::mapToQuestionRequest)
                .toList();
    }

    /**
     * Updates an existing question from admin edits.
     */
    public void updateQuestion(Integer id, QuestionRequest request) {
        Optional<Question> optionalQuestion = questionRepository.findById(id);
        if (optionalQuestion.isEmpty()) {
            throw new IllegalArgumentException("Question not found with id: " + id);
        }

        if (!request.hasValidCorrectAnswer()) {
            throw new IllegalArgumentException(
                    "Correct answer must match one of the provided options."
            );
        }

        List<String> options = request.getOptions()
                .stream()
                .map(String::trim)
                .toList();

        if (options.size() != 4) {
            throw new IllegalArgumentException("Exactly 4 options are required.");
        }

        Question question = optionalQuestion.get();
        question.setTitle(request.getQuestion().trim());
        question.setCategory(request.getCategory().trim());
        question.setRightAnswer(request.getCorrectAnswer().trim());
        question.setOption1(options.get(0));
        question.setOption2(options.get(1));
        question.setOption3(options.get(2));
        question.setOption4(options.get(3));

        questionRepository.save(question);
    }

    private QuestionRequest mapToQuestionRequest(Question question) {
        QuestionRequest dto = new QuestionRequest();

        dto.setQuestion(question.getTitle());
        dto.setCategory(question.getCategory());
        dto.setCorrectAnswer(question.getRightAnswer());
        dto.setOptions(List.of(
                question.getOption1(),
                question.getOption2(),
                question.getOption3(),
                question.getOption4()
        ));

        return dto;
    }
}