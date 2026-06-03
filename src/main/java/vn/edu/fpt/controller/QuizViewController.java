package vn.edu.fpt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.service.LessonService;

@Controller
@RequiredArgsConstructor
public class QuizViewController {

    private final LessonService lessonService;

    @Transactional
    @GetMapping("/quiz/lesson/{lessonId}")
    public String viewQuiz(@PathVariable("lessonId") Integer lessonId, Model model) {


    }
}
