import React, { useState } from "react";
import "./SignUp.css";
import { useNavigate, Link } from "react-router-dom";
import { signup } from "../api/auth"; // API 함수 주석 처리

const SignUp = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    username: "",
    password: "",
    passwordConfirm: "",
  });

  const [errors, setErrors] = useState({});
  const [successMessage, setSuccessMessage] = useState(""); // 성공 메시지
  const [errorMessage, setErrorMessage] = useState(""); // 실패 메시지

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrors({});
    setSuccessMessage("");
    setErrorMessage("");

    // 유효성 검사
    const newErrors = {};
    if (!formData.email) {
      newErrors.email = "이메일을 입력해주세요";
    }
    if (!formData.username) {
      newErrors.username = "사용자명을 입력해주세요";
    }
    if (!formData.password) {
      newErrors.password = "비밀번호를 입력해주세요";
    } else if (formData.password !== formData.passwordConfirm) {
      newErrors.passwordConfirm = "비밀번호가 일치하지 않습니다";
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    // API 호출 대신 localStorage에 저장
    try {
      try {
        const response = await signup(
            formData.email,
            formData.username,
            formData.password
        );
        console.log('API 응답:', response);

        // 로그인 후 사용자 정보를 로컬 스토리지에 저장
        //localStorage.setItem("currentUser", JSON.stringify(response.data.userDto));

        setSuccessMessage(response.message || "회원가입이 완료되었습니다!");
        setTimeout(() => {
          navigate("/login"); // 회원가입 성공 시 로그인 페이지로 이동
        }, 2000);
      } catch (error) {
        setErrorMessage(error.message || "회원가입 중 오류가 발생했습니다.");
      }
    } catch (error) {
      setErrorMessage("회원가입 중 오류가 발생했습니다.");
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: "",
      }));
    }
  };

  return (
    <div className="signup-container">
      <div className="signup-card">
        <h2 className="signup-title">회원가입</h2>
        <form onSubmit={handleSubmit} className="signup-form" autoComplete="off">
          {/* 성공 및 오류 메시지 */}
          {successMessage && <div className="success-message">{successMessage}</div>}
          {errorMessage && <div className="error-message">{errorMessage}</div>}

          {/* 이메일 입력 */}
          <div className="form-group">
            <input
              type="email" 
              name="email"
              placeholder="이메일"
              value={formData.email}
              onChange={handleChange}
              className={`form-input ${errors.email ? "error" : ""}`}
              autoComplete="new-email"
              aria-autocomplete="none"
              readOnly
              onFocus={(e) => e.target.removeAttribute('readonly')}
            />
            {errors.email && <span className="error-message">{errors.email}</span>}
          </div>

          {/* 사용자명 입력 */}
          <div className="form-group">
            <input
              type="text"
              name="username"
              placeholder="사용자명"
              value={formData.username}
              onChange={handleChange}
              className={`form-input ${errors.username ? "error" : ""}`}
              autoComplete="new-username"
              aria-autocomplete="none"
              readOnly
              onFocus={(e) => e.target.removeAttribute('readonly')}
            />
            {errors.username && <span className="error-message">{errors.username}</span>}
          </div>

          {/* 비밀번호 입력 */}
          <div className="form-group">
            <input
              type="password"
              name="password"
              placeholder="비밀번호"
              value={formData.password}
              onChange={handleChange}
              className={`form-input ${errors.password ? "error" : ""}`}
              autoComplete="new-password"
              aria-autocomplete="none"
              readOnly
              onFocus={(e) => e.target.removeAttribute('readonly')}
            />
            {errors.password && <span className="error-message">{errors.password}</span>}
          </div>

          {/* 비밀번호 확인 */}
          <div className="form-group">
            <input
              type="password"
              name="passwordConfirm"
              placeholder="비밀번호 확인"
              value={formData.passwordConfirm}
              onChange={handleChange}
              className={`form-input ${errors.passwordConfirm ? "error" : ""}`}
              autoComplete="new-password"
              aria-autocomplete="none"
              readOnly
              onFocus={(e) => e.target.removeAttribute('readonly')}
              
            />
            {errors.passwordConfirm && (
              <span className="error-message">{errors.passwordConfirm}</span>
            )}
          </div>

          {/* 회원가입 버튼 */}
          <button type="submit" className="signup-button">
            회원가입
          </button>

          {/* 로그인 링크 */}
          <div className="login-link">
            이미 계정이 있으신가요? <Link to="/login">로그인</Link>
          </div>
        </form>
      </div>
    </div>
  );
};

export default SignUp;
