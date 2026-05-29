package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.dto.StudentProgressReportDTO;
import java.util.HashSet;
import java.util.Set;
import com.tuition.new_tuition.entity.AppUser;
import com.tuition.new_tuition.entity.Exam;
import com.tuition.new_tuition.entity.ExamSubmission;
import com.tuition.new_tuition.entity.Question;
import com.tuition.new_tuition.entity.SubmissionAnswer;
import com.tuition.new_tuition.entity.Student;
import com.tuition.new_tuition.entity.Enrollment;
import com.tuition.new_tuition.entity.EnrollmentStatus;
import com.tuition.new_tuition.repository.QuestionRepository;
import com.tuition.new_tuition.repository.SubmissionAnswerRepository;
import com.tuition.new_tuition.repository.UserRepository;
import com.tuition.new_tuition.repository.StudentRepository;
import com.tuition.new_tuition.repository.EnrollmentRepository;
import com.tuition.new_tuition.service.ExamService;
import com.tuition.new_tuition.service.ExamSubmissionService;
import com.tuition.new_tuition.service.ProgressReportPdfService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student/exams")
public class StudentExamController {

    private final ExamService examService;
    private final ExamSubmissionService examSubmissionService;
    private final QuestionRepository questionRepository;
    private final SubmissionAnswerRepository submissionAnswerRepository;
    private final UserRepository userRepository;
    private final ProgressReportPdfService progressReportPdfService;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public StudentExamController(ExamService examService,
                                 ExamSubmissionService examSubmissionService,
                                 QuestionRepository questionRepository,
                                 SubmissionAnswerRepository submissionAnswerRepository,
                                 UserRepository userRepository,
                                 ProgressReportPdfService progressReportPdfService,
                                 StudentRepository studentRepository,
                                 EnrollmentRepository enrollmentRepository) {
        this.examService = examService;
        this.examSubmissionService = examSubmissionService;
        this.questionRepository = questionRepository;
        this.submissionAnswerRepository = submissionAnswerRepository;
        this.userRepository = userRepository;
        this.progressReportPdfService = progressReportPdfService;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }



    @GetMapping
    public String listStudentExams(
            @RequestParam(value = "questionType", required = false, defaultValue = "ALL") String questionType,
            Model model,
            HttpSession session) {
        String sessionUsername = (String) session.getAttribute("studentUsername");
        System.out.println("[ExamFilter] Session username: " + sessionUsername);

        Student loggedStudent = studentRepository.findByUsernameIgnoreCase(sessionUsername).orElse(null);
        if (loggedStudent == null) {
            System.out.println("[ExamFilter] Student not found in DB for username: " + sessionUsername);
            return "redirect:/student/login";
        }
        System.out.println("[ExamFilter] Student found: ID=" + loggedStudent.getId() + ", email=" + loggedStudent.getEmail());

        // Resolve AppUser ID for exam submission queries (linked to user_id in DB)
        AppUser appUser = userRepository.findByEmailIgnoreCase(loggedStudent.getEmail()).orElse(null);
        Long queryUserId = (appUser != null) ? appUser.getId() : loggedStudent.getId();
        System.out.println("[ExamFilter] AppUser lookup for email '" + loggedStudent.getEmail() + "': " + (appUser != null ? "found (ID=" + appUser.getId() + ")" : "not found, using student ID"));

        // 1. Fetch APPROVED enrollments only
        List<Enrollment> enrollments = enrollmentRepository
                .findByStudent_IdAndEnrollmentStatus(loggedStudent.getId(), EnrollmentStatus.APPROVED);
        System.out.println("[ExamFilter] APPROVED enrollments found: " + enrollments.size());

        // 2. Build two lookup sets for matching:
        //    Set A: exact batch names (lowercase) — e.g. "ma10-b1"
        //    Set B: (subject + "||" + normalizedGradeDigits) — e.g. "mathematics||10"
        //           gradeDigits are extracted so both "Grade 10" and "10" map to "10"
        Set<String> enrolledBatchNames = new HashSet<>();
        Set<String> enrolledSubjectGradeKeys = new HashSet<>();

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getBatch() == null) {
                System.out.println("[ExamFilter] WARNING: Enrollment ID=" + enrollment.getEnrollmentId() + " has null batch");
                continue;
            }

            String batchName = enrollment.getBatch().getBatchName();
            if (batchName != null) {
                enrolledBatchNames.add(batchName.trim().toLowerCase());
            }

            if (enrollment.getBatch().getCourse() != null) {
                String courseSub = enrollment.getBatch().getCourse().getSubject();
                String courseGrd = enrollment.getBatch().getCourse().getGrade();
                System.out.println("[ExamFilter]   Enrollment batch='" + batchName + "', courseSub='" + courseSub + "', courseGrade='" + courseGrd + "'");
                if (courseSub != null && courseGrd != null) {
                    // Normalize grade: extract only digits so "Grade 10", "10", "10th" → "10"
                    String gradeDigits = courseGrd.replaceAll("[^0-9]", "");
                    if (!gradeDigits.isEmpty()) {
                        enrolledSubjectGradeKeys.add(courseSub.trim().toLowerCase() + "||" + gradeDigits);
                    }
                    // Also store the full grade string for exact match fallback
                    enrolledSubjectGradeKeys.add(courseSub.trim().toLowerCase() + "||fullgrade||" + courseGrd.trim().toLowerCase());
                }
            } else {
                System.out.println("[ExamFilter]   Enrollment batch='" + batchName + "' has no course");
            }
        }

        System.out.println("[ExamFilter] Enrolled batch names: " + enrolledBatchNames);
        System.out.println("[ExamFilter] Enrolled subject+grade keys: " + enrolledSubjectGradeKeys);

        // 3. Filter all exams against the student's enrolled batches/courses
        List<Exam> allExams = examService.findAll();
        System.out.println("[ExamFilter] Total exams in DB: " + allExams.size());

        List<Exam> enrollmentFilteredExams = allExams.stream()
                .filter(exam -> {
                    String examSub = exam.getSubject();
                    String examBatch = exam.getBatchName();
                    if (examSub == null || examBatch == null) return false;

                    String examBatchKey   = examBatch.trim().toLowerCase();
                    String examSubjectKey = examSub.trim().toLowerCase();

                    // Primary: exact batch name match (e.g. exam.batchName = "MA10-B1")
                    if (enrolledBatchNames.contains(examBatchKey)) {
                        System.out.println("[ExamFilter] MATCH (batch) exam=" + exam.getId() + " subject='" + examSub + "' batch='" + examBatch + "'");
                        return true;
                    }

                    // Fallback A: normalize exam batchName digits and compare to subject+grade digit key
                    // Handles: exam.batchName="Grade 10", course.grade="10" → both → "10"
                    String examBatchDigits = examBatch.replaceAll("[^0-9]", "");
                    if (!examBatchDigits.isEmpty()) {
                        String candidateKey = examSubjectKey + "||" + examBatchDigits;
                        if (enrolledSubjectGradeKeys.contains(candidateKey)) {
                            System.out.println("[ExamFilter] MATCH (subject+gradeDigits) exam=" + exam.getId() + " subject='" + examSub + "' batch='" + examBatch + "' digits='" + examBatchDigits + "'");
                            return true;
                        }
                    }

                    // Fallback B: exact (subject + full batchName string) match
                    String fullGradePair = examSubjectKey + "||fullgrade||" + examBatchKey;
                    if (enrolledSubjectGradeKeys.contains(fullGradePair)) {
                        System.out.println("[ExamFilter] MATCH (subject+fullgrade) exam=" + exam.getId() + " subject='" + examSub + "' batch='" + examBatch + "'");
                        return true;
                    }

                    System.out.println("[ExamFilter] NO MATCH exam=" + exam.getId() + " subject='" + examSub + "' batch='" + examBatch + "' batchDigits='" + examBatch.replaceAll("[^0-9]", "") + "'");
                    return false;
                })
                .collect(Collectors.toList());

        System.out.println("[ExamFilter] Enrollment-filtered exams count: " + enrollmentFilteredExams.size());

        // 4. Build per-exam type map: examId → "MCQ" / "ESSAY" / "MIXED" / "NONE"
        //    Used by the template for type-badge display and by the filter below.
        Map<Long, String> examTypeMap = new HashMap<>();
        for (Exam exam : enrollmentFilteredExams) {
            List<com.tuition.new_tuition.entity.Question> questions = questionRepository.findByExamId(exam.getId());
            boolean hasMCQ   = questions.stream().anyMatch(q -> "MCQ".equalsIgnoreCase(q.getType()));
            boolean hasEssay = questions.stream().anyMatch(q -> "ESSAY".equalsIgnoreCase(q.getType()));
            String typeLabel;
            if (hasMCQ && hasEssay) typeLabel = "MIXED";
            else if (hasMCQ)        typeLabel = "MCQ";
            else if (hasEssay)      typeLabel = "ESSAY";
            else                    typeLabel = "NONE";
            examTypeMap.put(exam.getId(), typeLabel);
        }

        // 5. Apply question-type filter on top of enrollment-filtered list.
        //    Physical exams with no questions appear in ALL mode only.
        String normalizedFilter = (questionType == null || questionType.isBlank()) ? "ALL" : questionType.trim().toUpperCase();
        List<Exam> filteredExams;
        if ("MCQ".equals(normalizedFilter)) {
            filteredExams = enrollmentFilteredExams.stream()
                    .filter(e -> { String t = examTypeMap.getOrDefault(e.getId(), "NONE"); return "MCQ".equals(t) || "MIXED".equals(t); })
                    .collect(Collectors.toList());
        } else if ("ESSAY".equals(normalizedFilter)) {
            filteredExams = enrollmentFilteredExams.stream()
                    .filter(e -> { String t = examTypeMap.getOrDefault(e.getId(), "NONE"); return "ESSAY".equals(t) || "MIXED".equals(t); })
                    .collect(Collectors.toList());
        } else {
            filteredExams = enrollmentFilteredExams;
        }
        System.out.println("[ExamFilter] After questionType='" + normalizedFilter + "' filter: " + filteredExams.size());

        // 6. Build submitted-status map
        Map<Long, Boolean> submittedMap = new HashMap<>();
        for (Exam exam : filteredExams) {
            Optional<ExamSubmission> submission = examSubmissionService.findByExamIdAndStudentId(exam.getId(), queryUserId);
            submittedMap.put(exam.getId(), submission.isPresent());
        }

        // 7. Group by subject for the template
        Map<String, List<Exam>> examsBySubject = filteredExams.stream()
                .filter(e -> e.getSubject() != null)
                .collect(Collectors.groupingBy(e -> e.getSubject().trim()));

        model.addAttribute("exams", filteredExams);
        model.addAttribute("submittedMap", submittedMap);
        model.addAttribute("examsBySubject", examsBySubject);
        model.addAttribute("examTypeMap", examTypeMap);
        model.addAttribute("questionType", normalizedFilter);

        return "student-exam-list";
    }


    // -----------------------------------------------------------------------
    // DIAGNOSTIC ENDPOINT — visit /student/exams/debug to inspect live data
    // Shows the logged-in student's enrollments and all exams, revealing any
    // field-value mismatches that cause the filtering to return zero results.
    // REMOVE or @Profile("dev") this before production deployment.
    // -----------------------------------------------------------------------
    @GetMapping("/debug")
    @ResponseBody
    public String debugExamFilter(HttpSession session) {
        String sessionUsername = (String) session.getAttribute("studentUsername");
        StringBuilder sb = new StringBuilder();
        sb.append("<pre style='font-family:monospace;font-size:13px'>");
        sb.append("<b>=== EXAM FILTER DEBUG ===</b>\n\n");
        sb.append("Session studentUsername: ").append(sessionUsername).append("\n\n");

        if (sessionUsername == null) {
            sb.append("ERROR: No studentUsername in session. Are you logged in as a student?\n");
            sb.append("</pre>");
            return sb.toString();
        }

        Student loggedStudent = studentRepository.findByUsernameIgnoreCase(sessionUsername).orElse(null);
        if (loggedStudent == null) {
            sb.append("ERROR: Student not found in DB for username '").append(sessionUsername).append("'\n");
            sb.append("</pre>");
            return sb.toString();
        }
        sb.append("Student: ID=").append(loggedStudent.getId())
          .append(", name=").append(loggedStudent.getFullName())
          .append(", email=").append(loggedStudent.getEmail()).append("\n\n");

        List<Enrollment> allEnrollments = enrollmentRepository.findByStudent_Id(loggedStudent.getId());
        sb.append("<b>ALL ENROLLMENTS (").append(allEnrollments.size()).append("):</b>\n");
        for (Enrollment e : allEnrollments) {
            sb.append("  EnrollmentID=").append(e.getEnrollmentId())
              .append(" | Status=").append(e.getEnrollmentStatus());
            if (e.getBatch() != null) {
                sb.append(" | Batch='").append(e.getBatch().getBatchName()).append("'");
                if (e.getBatch().getCourse() != null) {
                    sb.append(" | CourseID='").append(e.getBatch().getCourse().getCourseId()).append("'");
                    sb.append(" | Subject='").append(e.getBatch().getCourse().getSubject()).append("'");
                    sb.append(" | Grade='").append(e.getBatch().getCourse().getGrade()).append("'");
                } else {
                    sb.append(" | Course=NULL");
                }
            } else {
                sb.append(" | Batch=NULL");
            }
            sb.append("\n");
        }

        sb.append("\n<b>ALL EXAMS IN DB:</b>\n");
        for (Exam exam : examService.findAll()) {
            sb.append("  ExamID=").append(exam.getId())
              .append(" | Name='").append(exam.getExamName()).append("'")
              .append(" | Subject='").append(exam.getSubject()).append("'")
              .append(" | BatchName='").append(exam.getBatchName()).append("'")
              .append(" | BatchNameDigits='").append(exam.getBatchName() != null ? exam.getBatchName().replaceAll("[^0-9]","") : "N/A").append("'")
              .append("\n");
        }

        sb.append("\n<b>MATCH ANALYSIS (APPROVED enrollments vs Exams):</b>\n");
        List<Enrollment> approvedEnrollments = allEnrollments.stream()
            .filter(e -> e.getEnrollmentStatus() == EnrollmentStatus.APPROVED)
            .collect(Collectors.toList());
        sb.append("APPROVED enrollment count: ").append(approvedEnrollments.size()).append("\n");

        Set<String> batchKeys = new HashSet<>();
        Set<String> subjectGradeKeys = new HashSet<>();
        for (Enrollment e : approvedEnrollments) {
            if (e.getBatch() != null) {
                if (e.getBatch().getBatchName() != null)
                    batchKeys.add(e.getBatch().getBatchName().trim().toLowerCase());
                if (e.getBatch().getCourse() != null) {
                    String s = e.getBatch().getCourse().getSubject();
                    String g = e.getBatch().getCourse().getGrade();
                    if (s != null && g != null) {
                        String digits = g.replaceAll("[^0-9]","");
                        if (!digits.isEmpty()) subjectGradeKeys.add(s.trim().toLowerCase() + "||" + digits);
                        subjectGradeKeys.add(s.trim().toLowerCase() + "||fullgrade||" + g.trim().toLowerCase());
                    }
                }
            }
        }
        sb.append("Enrolled batch name keys: ").append(batchKeys).append("\n");
        sb.append("Enrolled subject+grade keys: ").append(subjectGradeKeys).append("\n\n");

        for (Exam exam : examService.findAll()) {
            String es = exam.getSubject() != null ? exam.getSubject().trim().toLowerCase() : null;
            String eb = exam.getBatchName() != null ? exam.getBatchName().trim().toLowerCase() : null;
            String ebd = exam.getBatchName() != null ? exam.getBatchName().replaceAll("[^0-9]","") : "";
            boolean matched = false;
            String reason = "";
            if (es != null && eb != null) {
                if (batchKeys.contains(eb)) { matched = true; reason = "exact batch name"; }
                else if (!ebd.isEmpty() && subjectGradeKeys.contains(es + "||" + ebd)) { matched = true; reason = "subject+gradeDigits"; }
                else if (subjectGradeKeys.contains(es + "||fullgrade||" + eb)) { matched = true; reason = "subject+fullgrade"; }
            }
            sb.append("  Exam ").append(exam.getId()).append(" '").append(exam.getExamName())
              .append("' subject='").append(exam.getSubject()).append("' batch='").append(exam.getBatchName()).append("' → ")
              .append(matched ? "<span style='color:green'>MATCH (" + reason + ")</span>" : "<span style='color:red'>NO MATCH</span>")
              .append("\n");
        }

        sb.append("</pre>");
        return sb.toString();
    }

    @GetMapping("/start/{examId}")
    public String startExam(@PathVariable("examId") Long examId, Model model, HttpSession session) {
        String sessionUsername = (String) session.getAttribute("studentUsername");

        Student loggedStudent = studentRepository.findByUsernameIgnoreCase(sessionUsername).orElse(null);
        if (loggedStudent == null) return "redirect:/student/login";

        AppUser appUser = userRepository.findByEmailIgnoreCase(loggedStudent.getEmail()).orElse(null);
        Long queryUserId = (appUser != null) ? appUser.getId() : loggedStudent.getId();

        Optional<ExamSubmission> existing = examSubmissionService.findByExamIdAndStudentId(examId, queryUserId);
        if (existing.isPresent()) {
            return "redirect:/student/exams";
        }

        Exam exam = examService.findById(examId);
        List<Question> questions = questionRepository.findByExamId(examId);

        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);
        return "student-exam-start";
    }

    @PostMapping("/{examId}/submit")
    public String submitExam(@PathVariable("examId") Long examId, @RequestParam Map<String, String> params, HttpSession session) {
        String sessionUsername = (String) session.getAttribute("studentUsername");

        Student loggedStudent = studentRepository.findByUsernameIgnoreCase(sessionUsername).orElse(null);
        if (loggedStudent == null) return "redirect:/student/login";

        AppUser student = userRepository.findByEmailIgnoreCase(loggedStudent.getEmail())
                .orElseThrow(() -> new RuntimeException("Logged student does not have a corresponding User account. Please contact administrator."));

        Optional<ExamSubmission> existing = examSubmissionService.findByExamIdAndStudentId(examId, student.getId());
        if (existing.isPresent()) {
            return "redirect:/student/exams";
        }

        Exam exam = examService.findById(examId);

        List<Question> questions = questionRepository.findByExamId(examId);

        ExamSubmission submission = new ExamSubmission();
        submission.setExam(exam);
        submission.setStudent(student);

        int totalScore = 0;
        int maxTotalMarks = 0;
        boolean hasEssay = false;

        for (Question q : questions) {
            SubmissionAnswer answer = new SubmissionAnswer();
            answer.setSubmission(submission);
            answer.setQuestion(q);

            String selected = params.get("q_" + q.getId());
            answer.setAnswerText(selected);

            int qMarks = q.getMarks() != null ? q.getMarks() : 0;
            maxTotalMarks += qMarks;

            if ("MCQ".equalsIgnoreCase(q.getType())) {
                if (selected != null && selected.equalsIgnoreCase(q.getCorrectOption())) {
                    answer.setAwardedMarks(qMarks);
                    totalScore += qMarks;
                } else {
                    answer.setAwardedMarks(0);
                }
            } else {
                hasEssay = true;
                answer.setAwardedMarks(0); // Needs manual grading
            }

            submission.getAnswers().add(answer);
        }

        submission.setScore(totalScore);
        submission.setTotalMarks(maxTotalMarks);
        submission.setPassMark((int) Math.ceil(maxTotalMarks * 0.5));

        if (hasEssay) {
            submission.setStatus("PENDING");
        } else {
            submission.setStatus(totalScore >= submission.getPassMark() ? "PASS" : "FAIL");
        }

        examSubmissionService.save(submission);
        return "redirect:/student/exams/results/" + submission.getId();
    }

    @GetMapping("/results")
    public String resultHistory(Model model, HttpSession session) {
        String sessionUsername = (String) session.getAttribute("studentUsername");

        Student loggedStudent = studentRepository.findByUsernameIgnoreCase(sessionUsername).orElse(null);
        if (loggedStudent == null) return "redirect:/student/login";

        AppUser appUser = userRepository.findByEmailIgnoreCase(loggedStudent.getEmail()).orElse(null);
        Long queryUserId = (appUser != null) ? appUser.getId() : loggedStudent.getId();

        List<ExamSubmission> submissions = examSubmissionService.findByStudentId(queryUserId);

        model.addAttribute("submissions", submissions);
        return "student-result-history";
    }

    @GetMapping("/results/{submissionId}")
    public String viewResult(@PathVariable("submissionId") Long submissionId, Model model, HttpSession session) {
        String sessionUsername = (String) session.getAttribute("studentUsername");

        Student loggedStudent = studentRepository.findByUsernameIgnoreCase(sessionUsername).orElse(null);
        if (loggedStudent == null) return "redirect:/student/login";

        AppUser appUser = userRepository.findByEmailIgnoreCase(loggedStudent.getEmail()).orElse(null);
        Long queryUserId = (appUser != null) ? appUser.getId() : loggedStudent.getId();

        ExamSubmission submission = examSubmissionService.findById(submissionId);

        if (!submission.getStudent().getId().equals(queryUserId)) {
            return "redirect:/student/exams/results";
        }

        List<SubmissionAnswer> answers = submissionAnswerRepository.findBySubmissionId(submissionId);

        model.addAttribute("submission", submission);
        model.addAttribute("exam", submission.getExam());
        model.addAttribute("answers", answers);
        model.addAttribute("score", submission.getScore());
        model.addAttribute("totalMarks", submission.getTotalMarks());
        model.addAttribute("status", submission.getStatus());

        int passMark = (int) Math.ceil(submission.getTotalMarks() * 0.5);
        model.addAttribute("passMark", passMark);

        return "student-exam-result";
    }

    @GetMapping("/results/download-report")
    public ResponseEntity<byte[]> downloadStudentReport(HttpSession session) {
        String sessionUsername = (String) session.getAttribute("studentUsername");

        Student loggedStudent = studentRepository.findByUsernameIgnoreCase(sessionUsername).orElse(null);
        if (loggedStudent == null) {
            return ResponseEntity.status(404).build();
        }

        AppUser appUser = userRepository.findByEmailIgnoreCase(loggedStudent.getEmail()).orElse(null);
        Long queryUserId = (appUser != null) ? appUser.getId() : loggedStudent.getId();

        StudentProgressReportDTO report = examSubmissionService.getStudentProgressReport(queryUserId);
        byte[] pdfBytes = progressReportPdfService.generateStudentProgressReport(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("student-progress-report.pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
