// MyPage.js

import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./MyPage.css";

const MyPage = () => {
  const navigate = useNavigate();
  const [userData, setUserData] = useState(null);
  const [reservations, setReservations] = useState([]);

  useEffect(() => {
    // 로그인 상태 확인
    const user = JSON.parse(localStorage.getItem("currentUser"));
    if (!user) {
      navigate("/login");
      return;
    }
    setUserData(user);

    // 예약 정보 불러오기
    const userReservations = JSON.parse(
      localStorage.getItem("reservations") || "[]"
    );
    setReservations(userReservations);
  }, [navigate]);

  // 예약 삭제 함수
  const handleDeleteReservation = (indexToDelete) => {
    const isConfirmed = window.confirm("예약을 취소하시겠습니까?");

    if (isConfirmed) {
      // 현재 사용자의 예약만 필터링하여 해당 예약 제외
      const updatedReservations = reservations.filter(
        (_, index) => index !== indexToDelete
      );

      // localStorage 업데이트
      localStorage.setItem("reservations", JSON.stringify(updatedReservations));

      // 상태 업데이트
      setReservations(updatedReservations);

      alert("예약이 취소되었습니다.");
    }
  };

  return (
    <div className="mypage-container">
      <h1>마이페이지</h1>

      {userData && (
        <div className="user-info">
          <h2>회원 정보</h2>
          <div className="info-item">
            <span>이메일:</span>
            <span>{userData.email}</span>
          </div>
          <div className="info-item">
            <span>이름:</span>
            <span>{userData.name}</span>
          </div>
        </div>
      )}

      <div className="reservations">
        <h2>예약 내역</h2>
        {reservations.length > 0 ? (
          <div className="reservation-list">
            {reservations.map((reservation, index) => (
              <div key={index} className="reservation-item">
                <div className="reservation-info">
                  <h3>{reservation.restaurant}</h3>
                  <p>날짜: {reservation.date}</p>
                  <p>시간: {reservation.time}</p>
                  <p>인원: {reservation.people}명</p>
                </div>
                <button
                  className="delete-button"
                  onClick={() => handleDeleteReservation(index)}
                >
                  예약 취소
                </button>
              </div>
            ))}
          </div>
        ) : (
          <p className="no-reservations">예약 내역이 없습니다.</p>
        )}
      </div>
    </div>
  );
};

export default MyPage;
