package vn.edu.fpt.service.quiz;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.quizdto.QuizAttemptDTO;
import vn.edu.fpt.entity.*;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.QuizAttemptAnswerRepository;
import vn.edu.fpt.repository.QuizAttemptRepository;
import vn.edu.fpt.repository.QuizRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class QuizAttemptService {
    private final QuizAttemptRepository repository;
    private final QuizRepository quizRepository;
    private final QuizAttemptAnswerRepository quizAttemptAnswerRepository;
    private final DtoMapper dtoMapper;

    public QuizAttemptService(QuizAttemptRepository quizAttemptRepository, QuizRepository quizRepository, QuizAttemptAnswerRepository quizAttemptAnswerRepository, DtoMapper dtoMapper)
    {
        this.repository = quizAttemptRepository;
        this.quizRepository = quizRepository;
        this.quizAttemptAnswerRepository = quizAttemptAnswerRepository;
        this.dtoMapper = dtoMapper;
    }

    public QuizAttempt submitQuiz(Integer quizId, User user, Map<String, String[]> parameterMap) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Không thấy Quiz"));

        int totalPoints = 0;
        int userPoints = 0;
        List<QuizAttemptAnswer> attemptAnswers = new ArrayList<>();

        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .score(BigDecimal.ZERO)
                .passed(false)
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .submittedAt(LocalDateTime.now())
                .build();

        QuizAttempt savedAttempt = repository.save(attempt);

        for (QuizQuestion question : quiz.getQuestions()) {
            int qPoints = question.getPoints() != null ? question.getPoints() : 1;
            totalPoints += qPoints;

            List<Integer> correctAnswers = new ArrayList<>();
            for (QuizAnswer answer : question.getAnswers()) {
                if (Boolean.TRUE.equals(answer.getCorrect())) {
                    correctAnswers.add(answer.getId());
                }
            }
            Collections.sort(correctAnswers);

            String[] paramValues = parameterMap.get("quiz-" + quizId + "-question-" + question.getId());
            List<Integer> userSelected = new ArrayList<>();
            if (paramValues != null) {
                for (String val : paramValues) {
                    try {
                        Integer ansId = Integer.parseInt(val);
                        userSelected.add(ansId);

                        QuizAnswer selectedAns = question.getAnswers().stream()
                                .filter(a -> a.getId().equals(ansId))
                                .findFirst().orElse(null);

                        boolean isOptionCorrect = correctAnswers.contains(ansId);

                        QuizAttemptAnswer qaa = QuizAttemptAnswer.builder()
                                .attempt(savedAttempt)
                                .question(question)
                                .selectedAnswer(selectedAns)
                                .isCorrect(isOptionCorrect)
                                .build();
                        attemptAnswers.add(qaa);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            Collections.sort(userSelected);

            boolean isCorrect = userSelected.equals(correctAnswers);
            if (isCorrect) {
                userPoints += qPoints;
            }
        }

        double scorePercentage = totalPoints > 0 ? ((double) userPoints * 100.0 / totalPoints) : 0.0;
        boolean passed = scorePercentage >= quiz.getPassScorePercent();

        savedAttempt.setScore(BigDecimal.valueOf(scorePercentage));
        savedAttempt.setPassed(passed);

        if (!attemptAnswers.isEmpty()) {
            quizAttemptAnswerRepository.saveAll(attemptAnswers);
            savedAttempt.setAttemptAnswers(attemptAnswers);
        }

        return repository.save(savedAttempt);
    }

    public List<QuizAttempt> findAll() {
        return repository.findAll();
    }

    public Optional<QuizAttempt> findById(Integer id) {
        return repository.findById(id);
    }

    public QuizAttempt save(QuizAttempt entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public List<QuizAttempt> findAttemptsByUserAndQuiz(Integer userId, Integer quizId) {
        return repository.findByUserIdAndQuizIdOrderBySubmittedAtDesc(userId, quizId);
    }

    public Page<QuizAttemptDTO> getAllAttemptByQuizId(
            Integer quizId,
            int page,
            int size,
            String sortBy,
            String searchKeyword,
            String status // <-- Nhận thêm parameter status từ Controller
    ) {
        Sort sort;
        switch (sortBy != null ? sortBy : "") {
            case "scoreDesc":
                sort = Sort.by("score").descending();
                break;
            case "scoreAsc":
                sort = Sort.by("score").ascending();
                break;
            case "startedAtAsc":
                sort = Sort.by("startedAt").ascending();
                break;
            default:
                sort = Sort.by("submittedAt").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<QuizAttempt> attempts;

        // Xử lý chuyển đổi String status -> Boolean passed
        Boolean passedStatus = null;
        if ("passed".equalsIgnoreCase(status)) {
            passedStatus = true;
        } else if ("failed".equalsIgnoreCase(status)) {
            passedStatus = false;
        }

        boolean hasKeyword = (searchKeyword != null && !searchKeyword.trim().isEmpty());
        boolean hasStatus = (passedStatus != null);

        // Rẽ 4 nhánh điều kiện để gọi Repo tương ứng
        if (hasKeyword && hasStatus) {
            attempts = repository.searchAttemptsWithStatus(quizId, searchKeyword.trim(), passedStatus, pageable);
        } else if (hasKeyword) {
            attempts = repository.searchAttempts(quizId, searchKeyword.trim(), pageable);
        } else if (hasStatus) {
            attempts = repository.findAllByQuizIdAndPassed(quizId, passedStatus, pageable);
        } else {
            attempts = repository.findAllByQuizId(quizId, pageable);
        }

        List<QuizAttemptDTO> dtos = new ArrayList<>();
        for (QuizAttempt attempt : attempts.getContent()) {
            dtos.add(dtoMapper.toQuizAttemptDto(attempt));
        }

        return new PageImpl<>(dtos, attempts.getPageable(), attempts.getTotalElements());
    }
}
