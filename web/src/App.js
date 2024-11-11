import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import SimpleLogin from "./components/SimpleLogin";
import SignUp from "./components/SignUp";
import Reservation from "./components/Reservation";
import Main from "./components/Main";
import MyPage from "./components/Mypage";
import "./App.css";

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<Main />} />
          {/* 기본 경로를 로그인 페이지로 리다이렉트 */}

          {/* 로그인 페이지 */}
          <Route path="/login" element={<SimpleLogin />} />

          {/* 회원가입 페이지 */}
          <Route path="/signup" element={<SignUp />} />
          <Route path="/reservation" element={<Reservation />} />
          <Route path="/mypage" element={<MyPage />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
