package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.Feedback;
import vn.edu.fpt.repository.FeedbackRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FeedbackService {
    private final FeedbackRepository repository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.repository = feedbackRepository;
    }

    public List<Feedback> findAll() { return repository.findAll(); }
    public Optional<Feedback> findById(Integer id) { return repository.findById(id); }
    public Feedback save(Feedback entity) { return repository.save(entity); }
    public void deleteById(Integer id) { repository.deleteById(id); }
    public boolean existsById(Integer id) { return repository.existsById(id); }
    public boolean hasUserReviewedCourse(Integer userId, Integer courseId) {
        return repository.existsByUserIdAndCourseId(userId, courseId);
    }
}
