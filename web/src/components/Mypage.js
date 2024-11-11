import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
// import { fetchReservations, cancelReservation } from "../api/reservation"; // API 준비 완료 후 활성화
import "./MyPage.css";

const MyPage = () => {
  const navigate = useNavigate();
  const [userData, setUserData] = useState(null);
  const [reservations, setReservations] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    // 로그인 상태 확인
    const user = JSON.parse(localStorage.getItem("currentUser"));
    if (!user) {
      navigate("/login");
      return;
    }
    setUserData(user);
/*-------------------------------------------------------------------------*/
    // 백엔드 호출 대신 localStorage 사용 (임시 데이터)
    try {
      // const jwtToken = localStorage.getItem("jwtToken"); // API 호출 시 사용
      // const reservationsData = await fetchReservations(user.email, jwtToken); // API 호출
      const userReservations = JSON.parse(
        localStorage.getItem("reservations") || "[]"
      ); // 로컬에서 데이터 로드
      setReservations(userReservations); // 로컬 데이터를 상태에 저장
    } catch (err) {
      setError(err.message || "예약 정보를 불러오는 데 실패했습니다."); // 에러 처리
    }
  }, [navigate]);
/*--------------------------------------------------------------------------*/
// 예약 정보 조회 API 호출 시 아래 주석 부분 그대로 위에 대입 
/* 
    const fetchData = async () => {
      try {
        const jwtToken = localStorage.getItem("jwtToken");
        const data = await fetchReservations(user.email, jwtToken);
        setReservations(data);
      } catch (err) {
        setError(err.message);
      }
    };

    fetchData();
  }, [navigate]);

  if (error) {
    return <div>{error}</div>;
  }

  if (reservations.length === 0) {
    return <div>예약 내역이 없습니다.</div>;
  }
  */





  // 예약 삭제 함수
  const handleDeleteReservation = async (indexToDelete) => {
    const isConfirmed = window.confirm("예약을 취소하시겠습니까?");
    if (isConfirmed) {
      try {
        const reservationToDelete = reservations[indexToDelete]; // 삭제하려는 예약 데이터
        const jwtToken = localStorage.getItem("jwtToken"); // JWT 토큰 가져오기
  
        // 실제 API 호출 (예약 삭제)
        /*
        const response = await cancelReservation(reservationToDelete.reservationId, jwtToken);
        alert(response.message); // API 호출 성공 시 응답 메시지 출력
        */
  
        // 현재는 로컬 상태 업데이트만 수행
        const updatedReservations = reservations.filter(
          (_, index) => index !== indexToDelete
        );
  
        // 로컬 스토리지 업데이트
        localStorage.setItem("reservations", JSON.stringify(updatedReservations));
        setReservations(updatedReservations);
  
        alert("예약이 취소되었습니다.");
      } catch (error) {
        console.error("예약 취소 실패:", error.message);
        alert(`예약 취소에 실패했습니다: ${error.message}`);
      }
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
        {error ? (
          <p className="error-message" style={{ color: "red" }}>{error}</p>
        ) : reservations.length > 0 ? (
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
