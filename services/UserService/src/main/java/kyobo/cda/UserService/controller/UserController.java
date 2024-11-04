package kyobo.cda.UserService.controller;

import kyobo.cda.UserService.dto.UserDto;
import kyobo.cda.UserService.dto.UserSignUpRequestDto;
import kyobo.cda.UserService.dto.UserSignUpResponseDto;
import kyobo.cda.UserService.dto.UserUpdateRequestDto;
import kyobo.cda.UserService.entity.User;
import kyobo.cda.UserService.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserSignUpResponseDto> registerUser(@Validated @RequestBody UserSignUpRequestDto userSignUpRequestDto, BindingResult bindingResult, @RequestHeader("Authorization") String bearer) {

        if(bindingResult.hasErrors()){
            //400 에러 반환
            return new ResponseEntity<>(UserSignUpResponseDto.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("field error request").build(),
                    HttpStatus.BAD_REQUEST);
        }

        // 회원가입 이메일 인증 여부 검사
        String token = bearer.substring(7);
        if(!userSignUpRequestDto.getEmailVerified()) {
            // 이메일 인증이 완료되지 않은 경우 401 (Unauthorized) 응답 반환
            return new ResponseEntity<>(UserSignUpResponseDto.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("email not verified").build(),
                    HttpStatus.UNAUTHORIZED);
        }
        // 회원가입 토큰 검증
        if (!userService.validateSignUpToken(token)) {
            // 토큰이 유효하지 않은 경우 401 (Unauthorized) 응답 반환
            return new ResponseEntity<>(UserSignUpResponseDto.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("invalid token").build(),
                    HttpStatus.UNAUTHORIZED);
        }

        // 회원가입
        UserDto signupUser = userService.signupUser(userSignUpRequestDto);

        // 회원가입 완료
        return new ResponseEntity<>(UserSignUpResponseDto.builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("success signup")
                .userDto(signupUser)
                .build(), HttpStatus.CREATED);
    }

    // 사용자 정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
        UserDto userDto = userService.getUserById(id);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    // 사용자 정보 업데이트
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id, @Validated @RequestBody UserUpdateRequestDto request, BindingResult bindingResult) {
        UserDto userDto = userService.updateUser(id, request);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

}
