package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.Exam;
import com.tuition.new_tuition.service.ExamService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }


    @GetMapping({"", "/list"})
    public String list(@RequestParam(value = "subject", required = false) String subject,
                       @RequestParam(value = "batchName", required = false) String batchName,
                       @RequestParam(value = "examName", required = false) String examName,
                       @RequestParam(value = "examDate", required = false) String examDate,
                       Model model,
                       HttpSession session) {

        model.addAttribute("teacherId", session.getAttribute("teacherId"));

        List<Exam> allExams = examService.findAll();
        List<Exam> filteredExams = examService.filterExams(subject, batchName, examName, examDate);

        List<Exam> scienceExams = filteredExams.stream()
                .filter(e -> e.getSubject() != null && e.getSubject().equalsIgnoreCase("Science"))
                .collect(Collectors.toList());

        List<Exam> mathematicsExams = filteredExams.stream()
                .filter(e -> e.getSubject() != null && e.getSubject().equalsIgnoreCase("Mathematics"))
                .collect(Collectors.toList());

        model.addAttribute("exams", filteredExams);
        model.addAttribute("scienceExams", scienceExams);
        model.addAttribute("mathematicsExams", mathematicsExams);

        model.addAttribute("subjects", examService.getDistinctSubjects(allExams));
        model.addAttribute("batches", examService.getDistinctBatches(allExams));
        model.addAttribute("examNames", examService.getDistinctExamNames(allExams));

        model.addAttribute("selectedSubject", subject);
        model.addAttribute("selectedBatchName", batchName);
        model.addAttribute("selectedExamName", examName);
        model.addAttribute("selectedExamDate", examDate);

        return "exam-list";
    }

    @GetMapping("/dump")
    @ResponseBody
    public String dumpExams() {
        StringBuilder sb = new StringBuilder();
        for (Exam e : examService.findAll()) {
            sb.append("ID: ").append(e.getId())
              .append(" | Subject: ").append(e.getSubject())
              .append(" | BatchName (Grade): ").append(e.getBatchName())
              .append(" | Name: ").append(e.getExamName())
              .append("<br>");
        }
        return sb.toString();
    }

    @GetMapping("/add")
    public String showAddForm(Model model, HttpSession session) {
        model.addAttribute("exam", new Exam());
        return "exam-add";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("exam") Exam exam, HttpSession session) {
        examService.save(exam);
        return "redirect:/exams";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        model.addAttribute("exam", examService.findById(id));
        return "exam-edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Long id,
                         @ModelAttribute("exam") Exam exam,
                         HttpSession session) {
        examService.update(id, exam);
        return "redirect:/exams";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, HttpSession session) {
        examService.deleteById(id);
        return "redirect:/exams";
    }
}
