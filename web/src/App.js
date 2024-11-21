import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import SimpleLogin from "./components/SimpleLogin";
import SignUp from "./components/SignUp";
import Reservation from "./components/Reservation";
import Main from "./components/Main";
import MyPage from "./components/Mypage";
import ImageGallery from "./components/ImageGallery"; // 이미지 갤러리 추가
import "./App.css";

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<Main />} />
          {/* 기본 경로를 메인 페이지로 설정 */}

          {/* 로그인 페이지 */}
          <Route path="/login" element={<SimpleLogin />} />

          {/* 회원가입 페이지 */}
          <Route path="/signup" element={<SignUp />} />

          {/* 예약 페이지 */}
          <Route path="/reservation" element={<Reservation />} />

          {/* 마이페이지 */}
          <Route path="/mypage" element={<MyPage />} />

          {/* 이미지 갤러리 페이지 */}
          <Route path="/gallery" element={<ImageGallery />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
