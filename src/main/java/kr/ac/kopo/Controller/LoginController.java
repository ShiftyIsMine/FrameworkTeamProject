package kr.ac.kopo.Controller;


import kr.ac.kopo.entity.User;
import kr.ac.kopo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {

        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (logout != null) {
            model.addAttribute("message", "성공적으로 로그아웃되었습니다.");
        }

        return "login";
    }

    @GetMapping("/loginfailed")
    public String loginFailed(Model model) {
        model.addAttribute("error", "로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.");
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("error", "접근 권한이 없습니다.");
        return "error/403";
    }
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String username,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String fullName,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        try {
            // 사용자명 중복 확인
            if (userService.usernameExists(username)) {
                model.addAttribute("error", "이미 사용 중인 사용자명입니다.");
                return "signup";
            }

            // 이메일 중복 확인
            if (userService.emailExists(email)) {
                model.addAttribute("error", "이미 사용 중인 이메일입니다.");
                return "signup";
            }

            // 새 사용자 생성
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setFullName(fullName);
            user.setRole(User.Role.valueOf("USER"));
            user.setEnabled(true);

            userService.createUser(user);

            redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.");
            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "signup";
        }
    }
}