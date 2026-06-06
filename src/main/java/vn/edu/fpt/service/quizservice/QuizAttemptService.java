package vn.edu.fpt.service.quizservice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.QuizAttempt;
import vn.edu.fpt.repository.QuizAttemptRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QuizAttemptService {
    private final QuizAttemptRepository repository;

    public QuizAttemptService(QuizAttemptRepository quizAttemptRepository) {
        this.repository = quizAttemptRepository;
    }

    public List<QuizAttempt> findAll() { return repository.findAll(); }
    public Optional<QuizAttempt> findById(Integer id) { return repository.findById(id); }
    public QuizAttempt save(QuizAttempt entity) { return repository.save(entity); }
    public void deleteById(Integer id) { repository.deleteById(id); }
    public boolean existsById(Integer id) { return repository.existsById(id); }

    public List<QuizAttempt> findAttemptsByUserAndQuiz(Integer userId, Integer quizId) {
        return repository.findByUserIdAndQuizIdOrderBySubmittedAtDesc(userId, quizId);
    }
}
