import React, { useState, useEffect } from "react";
import "../css/SignUp.css";
import { useNavigate, Link } from "react-router-dom";

const SignUp = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    passwordConfirm: "",
    verificationCode: "",
  });

  const [errors, setErrors] = useState({});
  const [verificationStatus, setVerificationStatus] = useState({
    sent: false,
    verified: false,
    timer: 180, // 3분
  });
  const [isTimerRunning, setIsTimerRunning] = useState(false);

  useEffect(() => {
    let interval;
    if (isTimerRunning && verificationStatus.timer > 0) {
      interval = setInterval(() => {
        setVerificationStatus((prev) => ({
          ...prev,
          timer: prev.timer - 1,
        }));
      }, 1000);
    } else if (verificationStatus.timer === 0) {
      setIsTimerRunning(false);
    }
    return () => clearInterval(interval);
  }, [isTimerRunning, verificationStatus.timer]);

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, "0")}`;
  };

  const handleSendVerification = () => {
    if (!formData.email) {
      setErrors({ email: "이메일을 입력해주세요" });
      return;
    }
    // 실제 구현시에는 여기서 서버로 인증 코드 발송 요청
    const mockVerificationCode = "123456"; // 실제로는 서버에서 생성
    console.log("인증코드:", mockVerificationCode);

    setVerificationStatus((prev) => ({
      ...prev,
      sent: true,
      timer: 180,
    }));
    setIsTimerRunning(true);
    setErrors({});
  };

  const handleVerifyCode = () => {
    // 실제 구현시에는 서버로 인증 코드 확인 요청
    if (formData.verificationCode === "123456") {
      setVerificationStatus((prev) => ({
        ...prev,
        verified: true,
        sent: false,
      }));
      setIsTimerRunning(false);
      setErrors({});
    } else {
      setErrors((prev) => ({
        ...prev,
        verificationCode: "인증번호가 일치하지 않습니다",
      }));
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const newErrors = {};

    if (!formData.email) {
      newErrors.email = "이메일을 입력해주세요";
    }
    if (!formData.password) {
      newErrors.password = "비밀번호를 입력해주세요";
    }
    if (formData.password !== formData.passwordConfirm) {
      newErrors.passwordConfirm = "비밀번호가 일치하지 않습니다";
    }
    if (!verificationStatus.verified) {
      newErrors.verification = "이메일 인증이 필요합니다";
    }

    if (Object.keys(newErrors).length === 0) {
      const existingUsers = JSON.parse(localStorage.getItem("users") || "[]");

      if (existingUsers.some((user) => user.email === formData.email)) {
        setErrors({ email: "이미 존재하는 이메일입니다" });
        return;
      }

      const newUser = {
        email: formData.email,
        password: formData.password,
      };

      localStorage.setItem(
        "users",
        JSON.stringify([...existingUsers, newUser])
      );

      alert("회원가입이 완료되었습니다!");
      navigate("/login");
    } else {
      setErrors(newErrors);
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
        <form onSubmit={handleSubmit} className="signup-form">
          {/* 이메일 입력 */}
          <div className="form-group">
            <div className="email-group">
              <input
                type="email"
                name="email"
                placeholder="이메일"
                value={formData.email}
                onChange={handleChange}
                className={`form-input ${errors.email ? "error" : ""}`}
                disabled={verificationStatus.verified}
              />
              <button
                type="button"
                onClick={handleSendVerification}
                className="verification-button"
                disabled={verificationStatus.verified}
              >
                {verificationStatus.sent ? "재전송" : "인증"}
              </button>
            </div>
            {errors.email && (
              <span className="error-message">{errors.email}</span>
            )}
          </div>

          {/* 인증번호 입력 */}
          {verificationStatus.sent && !verificationStatus.verified && (
            <div className="form-group">
              <div className="email-group">
                <input
                  type="text"
                  name="verificationCode"
                  placeholder="인증번호 6자리"
                  value={formData.verificationCode}
                  onChange={handleChange}
                  className={`form-input ${
                    errors.verificationCode ? "error" : ""
                  }`}
                  maxLength={6}
                />
                <button
                  type="button"
                  onClick={handleVerifyCode}
                  className="verification-button"
                >
                  확인
                </button>
              </div>
              {verificationStatus.timer > 0 && (
                <span className="timer">
                  {formatTime(verificationStatus.timer)}
                </span>
              )}
              {errors.verificationCode && (
                <span className="error-message">{errors.verificationCode}</span>
              )}
            </div>
          )}

          {/* 비밀번호 입력 */}
          <div className="form-group">
            <input
              type="password"
              name="password"
              placeholder="비밀번호"
              value={formData.password}
              onChange={handleChange}
              className={`form-input ${errors.password ? "error" : ""}`}
            />
            {errors.password && (
              <span className="error-message">{errors.password}</span>
            )}
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
