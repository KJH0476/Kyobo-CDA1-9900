import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import "./SimpleLogin.css";
//import { login } from '../api/auth';

const SimpleLogin = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [error, setError] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();

    //{로그인 api 호출 시작부분}
    /*
    try {
      // API 호출 시도
      const response = await login(formData.email, formData.password);
      console.log('API 응답:', response);

      if (response.success) {
        // API 로그인 성공
        localStorage.setItem("currentUser", JSON.stringify(response.data));
        alert("로그인 성공!");
        navigate("/");
        setTimeout(() => {
          window.location.reload();
        }, 100);
      } else {
        // API 로그인 실패
        setError(response.message);
      }
    } catch (error) {
      console.error('로그인 에러:', error);
      // API 오류 발생
*/

    const users = JSON.parse(localStorage.getItem("users") || "[]");
    const user = users.find(
      (u) => u.email === formData.email && u.password === formData.password
    );

    if (user) {
      // 로그인 성공
      localStorage.setItem("currentUser", JSON.stringify(user));
      alert("로그인 성공!");
      navigate("/"); // 메인 페이지로 이동
      setTimeout(() => {
        window.location.reload(); // 페이지 자동 새로고침
      }, 100); // 약간의 지연 시간을 주어 navigate가 완료된 후 새로고침
    } else {
      // 로그인 실패
      setError("이메일 또는 비밀번호가 올바르지 않습니다");
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    setError(""); // 입력이 변경되면 에러 메시지 초기화
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h2 className="login-title">로그인</h2>
        <form onSubmit={handleSubmit} className="login-form">
          {error && <div className="error-message">{error}</div>}

          {/* 이메일 입력 */}
          <div className="form-group">
            <input
              type="email"
              name="email"
              placeholder="이메일"
              value={formData.email}
              onChange={handleChange}
              className="form-input"
              required
              autoComplete="new-password"
              aria-autocomplete="none"
              readOnly
              onFocus={(e) => e.target.removeAttribute('readonly')}
            />
          </div>

          {/* 비밀번호 입력 */}
          <div className="form-group">
            <input
              type="password"
              name="password"
              placeholder="비밀번호"
              value={formData.password}
              onChange={handleChange}
              className="form-input"
              required
              autoComplete="new-password"
              aria-autocomplete="none"
              readOnly
              onFocus={(e) => e.target.removeAttribute('readonly')}
            />
          </div>

          {/* 로그인 버튼 */}
          <button type="submit" className="login-button">
            로그인
          </button>

          {/* 추가 링크 */}
          <div className="additional-links">
            <Link to="/signup">회원가입</Link>
          </div>
        </form>
      </div>
    </div>
  );
};

export default SimpleLogin;
