package com.quizmicroservice.service;

import com.quizmicroservice.dto.QuestionRequest;
import com.quizmicroservice.model.Question;
import com.quizmicroservice.model.Quiz;
import com.quizmicroservice.repository.QuestionRepository;
import com.quizmicroservice.repository.QuizRepository;
import com.quizmicroservice.dto.QuizMetaResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public QuestionService(QuestionRepository questionRepository,
                           QuizRepository quizRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }
    
    public QuizMetaResponse getQuizMeta(
            String subject,
            String difficulty
    ) {
        long totalQuestions =
                questionRepository.countByQuiz_SubjectAndQuiz_Difficulty(
                        subject,
                        difficulty
                );

        int totalTimeMinutes =
                Math.max(5, (int) totalQuestions);

        return new QuizMetaResponse(
                subject,
                difficulty,
                totalQuestions,
                totalTimeMinutes
        );
    }
    
    public Page<QuestionRequest> getQuestionsForAdmin(
            int page,
            int size,
            String search
    ) {
        Pageable pageable = PageRequest.of(page, size);

        if (search != null && !search.isBlank()) {
            return questionRepository
            		.findByQuestionContainingIgnoreCaseOrQuiz_SubjectContainingIgnoreCaseOrQuiz_DifficultyContainingIgnoreCase(
                            search,
                            search,
                            search,
                            pageable
                    )
                    .map(this::mapToQuestionRequest);
        }

        return questionRepository
                .findAll(pageable)
                .map(this::mapToQuestionRequest);
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public List<Question> getQuestionsBySubject(String subject) {
        String normalizedSubject = normalizeSubject(subject);
        return questionRepository.findByQuiz_Subject(normalizedSubject);
    }

    public List<Question> getQuestionsBySubjectAndDifficulty(String subject, String difficulty) {
        String normalizedSubject = normalizeSubject(subject);
        String normalizedDifficulty = normalizeDifficulty(difficulty);

        return questionRepository.findByQuiz_SubjectAndQuiz_Difficulty(
                normalizedSubject,
                normalizedDifficulty
        );
    }

    public void addQuestion(QuestionRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question request is required.");
        }

        List<String> options = validateAndNormalizeOptions(request);
        String subject = normalizeSubject(request.getSubject());
        String difficulty = normalizeDifficulty(request.getDifficulty());

        Quiz quiz = quizRepository
                .findBySubjectAndDifficulty(subject, difficulty)
                .orElseGet(() -> {
                    Quiz newQuiz = new Quiz();
                    newQuiz.setSubject(subject);
                    newQuiz.setDifficulty(difficulty);
                    return quizRepository.save(newQuiz);
                });

        Question question = new Question();
        question.setQuiz(quiz);
        question.setQuestion(normalizeQuestionText(request.getQuestion()));
        question.setOption1(options.get(0));
        question.setOption2(options.get(1));
        question.setOption3(options.get(2));
        question.setOption4(options.get(3));
        question.setCorrectOption(request.getCorrectOption());

        questionRepository.save(question);
    }

    public void deleteQuestion(int id) {
        if (!questionRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Question not found with id: " + id
            );
        }

        questionRepository.deleteById(id);
    }

    public List<QuestionRequest> getAllForAdmin() {
        return questionRepository.findAll()
                .stream()
                .map(this::mapToQuestionRequest)
                .toList();
    }
    public void updateQuestion(Integer id, QuestionRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question request is required.");
        }

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Question not found with id: " + id
                ));

        List<String> options = validateAndNormalizeOptions(request);
        String subject = normalizeSubject(request.getSubject());
        String difficulty = normalizeDifficulty(request.getDifficulty());

        Quiz quiz = quizRepository
                .findBySubjectAndDifficulty(subject, difficulty)
                .orElseGet(() -> {
                    Quiz newQuiz = new Quiz();
                    newQuiz.setSubject(subject);
                    newQuiz.setDifficulty(difficulty);
                    return quizRepository.save(newQuiz);
                });

        question.setQuiz(quiz);
        question.setQuestion(normalizeQuestionText(request.getQuestion()));
        question.setOption1(options.get(0));
        question.setOption2(options.get(1));
        question.setOption3(options.get(2));
        question.setOption4(options.get(3));
        question.setCorrectOption(request.getCorrectOption());

        questionRepository.save(question);
    }

    private String normalizeSubject(String subject) {
        String normalized = subject != null ? subject.trim() : null;
        if (normalized == null || normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject is required.");
        }
        return normalized;
    }

    private String normalizeDifficulty(String difficulty) {
        String normalized = difficulty != null ? difficulty.trim().toUpperCase() : null;
        if (normalized == null || normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Difficulty is required.");
        }
        return normalized;
    }

    private String normalizeQuestionText(String question) {
        String normalized = question != null ? question.trim() : null;
        if (normalized == null || normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question is required.");
        }
        return normalized;
    }

    private List<String> validateAndNormalizeOptions(QuestionRequest request) {
        if (request.getOptions() == null || request.getOptions().size() != 4) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Exactly 4 options are required."
            );
        }

        List<String> options = request.getOptions()
                .stream()
                .map(option -> option != null ? option.trim() : null)
                .toList();

        boolean hasBlankOption = options.stream().anyMatch(option -> option == null || option.isBlank());
        if (hasBlankOption) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "All 4 options are required."
            );
        }

        if (!request.hasValidCorrectOption()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Correct option index must be between 1 and 4 and match the options list."
            );
        }

        return options;
    }

    private QuestionRequest mapToQuestionRequest(Question question) {
        QuestionRequest dto = new QuestionRequest();
        dto.setId(question.getId());
        dto.setQuestion(question.getQuestion());
        dto.setCorrectOption(question.getCorrectOption());
        dto.setOptions(List.of(
                question.getOption1(),
                question.getOption2(),
                question.getOption3(),
                question.getOption4()
        ));

        if (question.getQuiz() != null) {
            dto.setSubject(question.getQuiz().getSubject());
            dto.setDifficulty(question.getQuiz().getDifficulty());
        }

        return dto;
    }
}