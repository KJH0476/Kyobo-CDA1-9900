package kyobo.cda.UserService.controller;

import kyobo.cda.UserService.dto.LoginRequestDto;
import kyobo.cda.UserService.dto.LoginResponseDto;
import kyobo.cda.UserService.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Validated @RequestBody LoginRequestDto loginRequestDto, BindingResult bindingResult) {

        log.info("login {} {}", loginRequestDto.getEmail(), loginRequestDto.getPassword());
        if(bindingResult.hasErrors()){
            // 400 에러 반환
            return new ResponseEntity<>(LoginResponseDto.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("field error request").build(),
                    HttpStatus.BAD_REQUEST);
        }

        try {
            return new ResponseEntity<>(loginService.login(loginRequestDto), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(LoginResponseDto.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("member does not exist").build(),
                    HttpStatus.UNAUTHORIZED);
        }
    }
}
