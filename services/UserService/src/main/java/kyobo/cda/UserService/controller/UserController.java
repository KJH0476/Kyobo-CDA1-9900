package kyobo.cda.UserService.controller;

import kyobo.cda.UserService.dto.UserDto;
import kyobo.cda.UserService.dto.UserSignUpRequestDto;
import kyobo.cda.UserService.dto.UserSignUpResponseDto;
import kyobo.cda.UserService.dto.UserUpdateRequestDto;
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
    public ResponseEntity<UserSignUpResponseDto> registerUser(@Validated @RequestBody UserSignUpRequestDto userSignUpRequestDto, BindingResult bindingResult) {

        if(bindingResult.hasErrors()){
            //400 에러 반환
            return new ResponseEntity<>(UserSignUpResponseDto.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("field error request").build(),
                    HttpStatus.BAD_REQUEST);
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
    @GetMapping("/find/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID userId) {
        UserDto userDto = userService.getUserById(userId);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    // 사용자 정보 업데이트
    @PutMapping("/update/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId, @RequestBody UserUpdateRequestDto request) {
        UserDto userDto = userService.updateUser(userId, request);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<UserSignUpResponseDto> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(UserSignUpResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }
}
