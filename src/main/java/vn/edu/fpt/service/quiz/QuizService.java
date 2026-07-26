package vn.edu.fpt.service.quiz;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import vn.edu.fpt.dto.quizdto.QuizDTO;
import vn.edu.fpt.dto.quizdto.QuizQuestionDTO;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.entity.Quiz;
import vn.edu.fpt.entity.QuizAttempt;
import vn.edu.fpt.entity.QuizAttemptAnswer;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.QuizStatus;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.LessonRepository;
import vn.edu.fpt.repository.QuizAttemptAnswerRepository;
import vn.edu.fpt.repository.QuizAttemptRepository;
import vn.edu.fpt.repository.QuizQuestionRepository;
import vn.edu.fpt.repository.QuizRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class QuizService {
    private final QuizRepository repository;
    private final DtoMapper dtoMapper;
    private final LessonRepository lessonRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAttemptAnswerRepository quizAttemptAnswerRepository;

    public QuizService(QuizRepository quizRepository, DtoMapper dtoMapper, LessonRepository lessonRepository, QuizQuestionRepository quizQuestionRepository, QuizAttemptRepository quizAttemptRepository, QuizAttemptAnswerRepository quizAttemptAnswerRepository) {
        this.repository = quizRepository;
        this.dtoMapper = dtoMapper;
        this.lessonRepository = lessonRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizAttemptAnswerRepository = quizAttemptAnswerRepository;
    }

    public void populateLessonQuizModel(Integer lessonId, User user, boolean retake, Integer activeQuizId, Integer activeAttemptId, Model model) {
        Lesson lesson = lessonRepository.findByIdWithQuizzes(lessonId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lesson với id " + lessonId));
        List<QuizDTO> quizzes = dtoMapper.toQuizDtos(lesson.getQuizzes());
        int totalQuestions = totalQuestion(quizzes);
        Map<Integer, List<QuizAttempt>> quizAttemptsMap = new HashMap<>();
        List<Integer> allAttemptIds = new ArrayList<>();

        for (QuizDTO q : quizzes) {
            List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdAndQuizIdOrderBySubmittedAtDesc(user.getId(), q.getId());
            quizAttemptsMap.put(q.getId(), attempts);
            for (QuizAttempt att : attempts) {
                allAttemptIds.add(att.getId());
            }
        }

        Map<Integer, Set<Integer>> attemptSelectedAnswersMap = new HashMap<>();
        if (!allAttemptIds.isEmpty()) {
            List<QuizAttemptAnswer> attemptAnswers = quizAttemptAnswerRepository.findByAttemptIdIn(allAttemptIds);
            for (QuizAttemptAnswer qaa : attemptAnswers) {
                if (qaa.getAttempt() != null && qaa.getSelectedAnswer() != null) {
                    Integer attemptId = qaa.getAttempt().getId();
                    if (!attemptSelectedAnswersMap.containsKey(attemptId)) {
                        attemptSelectedAnswersMap.put(attemptId, new HashSet<>());
                    }
                    attemptSelectedAnswersMap.get(attemptId).add(qaa.getSelectedAnswer().getId());
                }
            }
        }

        Integer finalActiveQuizId = activeQuizId != null ? activeQuizId : (quizzes.isEmpty() ? null : quizzes.get(0).getId());

        QuizAttempt currentAttempt = null;
        if (activeAttemptId != null) {
            for (List<QuizAttempt> list : quizAttemptsMap.values()) {
                for (QuizAttempt att : list) {
                    if (att.getId().equals(activeAttemptId)) {
                        currentAttempt = att;
                        break;
                    }
                }
                if (currentAttempt != null) break;
            }
        }
        if (currentAttempt == null && finalActiveQuizId != null) {
            List<QuizAttempt> attempts = quizAttemptsMap.get(finalActiveQuizId);
            if (attempts != null && !attempts.isEmpty()) {
                currentAttempt = attempts.get(0);
            }
        }

        model.addAttribute("lessonId", lesson.getId());
        model.addAttribute("lessonTitle", lesson.getTitle());
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("quizCount", quizzes.size());
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("quizAttemptsMap", quizAttemptsMap);
        model.addAttribute("attemptSelectedAnswersMap", attemptSelectedAnswersMap);
        model.addAttribute("retake", retake);
        model.addAttribute("activeQuizId", finalActiveQuizId);
        model.addAttribute("activeAttemptId", activeAttemptId);
        model.addAttribute("currentAttempt", currentAttempt);
    }

    public List<Quiz> findAll() {
        return repository.findAll();
    }

    public Optional<Quiz> findById(Integer id) {
        return repository.findById(id);
    }

    public Quiz save(Quiz entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public int totalQuestion(List<QuizDTO> quizzes) {
        int total = 0;
        for (QuizDTO quizDTO : quizzes) {
            if (quizDTO.getQuestions() == null || quizDTO.getQuestions().isEmpty()) {
                continue;
            }
            total += quizDTO.getQuestions().size();
        }
        return total;
    }

    public QuizDTO createQuiz(
            Integer lessonId,
            QuizDTO request) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Lesson not found"));

        Quiz quiz = Quiz.builder()
                .lesson(lesson)
                .title(request.getTitle())
                .description(request.getDescription())
                .passScorePercent(request.getPassScorePercent())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .status(QuizStatus.DRAFT.name())
                .isRandomQuestion(
                        Boolean.TRUE.equals(
                                request.getIsRandomQuestion()))
                .isRandomAnswer(
                        Boolean.TRUE.equals(
                                request.getIsRandomAnswer()))
                .build();

        Quiz savedQuiz = repository.save(quiz);

        return dtoMapper.toQuizDto(savedQuiz);
    }

    public List<QuizDTO> getQuizzesByLesson(Integer lessonId) {

        List<Quiz> quizzes =
                repository.findByLessonId((lessonId));

        List<QuizDTO> result = new ArrayList<>();

        for (Quiz quiz : quizzes) {

            QuizDTO dto =
                    dtoMapper.toQuizDto(quiz);

            result.add(dto);
        }

        return result;
    }

    public QuizDTO findQuizById(Integer quizId){
        return dtoMapper.toQuizDto(repository.findQuizById(quizId));
    }

    public Page<QuizDTO> getQuizzesByStatus(int page, int size, String status, Integer lessonId){
        Pageable pageable = PageRequest.of(page, size);

        Page<Quiz> quizPage = repository.findByLessonIdAndStatus(lessonId,status, pageable);

        List<QuizDTO> dtos = new ArrayList<>();
        for (Quiz dto : quizPage.getContent()){
            dtos.add(dtoMapper.toQuizDto(dto));
        }

        return new PageImpl<>(
                dtos,
                quizPage.getPageable(),
                quizPage.getTotalElements()
        );

    }

    public void updateQuizMeta(QuizDTO quizDto){
        Quiz quiz = repository.findQuizById(quizDto.getId());
        if(quiz == null){
            System.out.println("Quiz not found !");
            return;
        }

        quiz.setTitle(quizDto.getTitle());
        quiz.setPassScorePercent(quizDto.getPassScorePercent());
        quiz.setTimeLimitMinutes(quizDto.getTimeLimitMinutes());


    }

    public void archived(Integer quizId){
        Quiz quiz = repository.findQuizById(quizId);
        if(quiz == null){
            System.out.println("Quiz not found !");
            return;
        }

        quiz.setStatus(QuizStatus.ARCHIVED.name());
    }

    public void saveDraft(Integer quizId){
        Quiz quiz = repository.findQuizById(quizId);
        if(quiz == null){
            System.out.println("Quiz not found !");
            return;
        }

        quiz.setStatus(QuizStatus.DRAFT.name());
    }

    public boolean publishQuiz(Integer quizId){
        Quiz quiz = repository.findQuizById(quizId);
        if(quiz == null){
            System.out.println("Quiz not found");
            return false;
        }

    long quizQuestionCount = quizQuestionRepository.countByQuizId(quizId);
        if(quizQuestionCount < 1){
            System.out.println("Quiz need at least 1 question to publish!");
            return false;
        }

        quiz.setStatus(QuizStatus.PUBLISHED.name());
        quiz.setPublishedAt(LocalDateTime.now());
        return true;
    }

    public void deleteQuiz(Integer quizId) {

        Quiz quiz = repository.findQuizById(quizId);
        if(quiz == null){
            System.out.println("Quit not found !");
            return;
        }

        repository.delete(quiz);
    }

    public Long getTotalQuizByLessonId(Integer lessonId){
        return repository.countByLessonId(lessonId);
    }

    public Long getTotalQuizByLessonIdAndStatus(Integer lessonId,String status){
        return repository.countByLessonIdAndStatus(lessonId, status);
    }



}
