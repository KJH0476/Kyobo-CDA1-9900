import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchReservations, cancelReservation } from "../api/reservation"; // // 예약 관련 API 함수들
import "./MyPage.css";

// 마이페이지 컴포넌트 정의
const MyPage = () => {
  const navigate = useNavigate(); // 페이지 이동을 위한 훅
  const [userData, setUserData] = useState(null); // 사용자 정보 상태
  const [reservations, setReservations] = useState([]); // 예약 목록 상태
  const [error, setError] = useState(null); // 에러 메시지 상태
  const [isLoading, setIsLoading] = useState(false); // 로딩 상태

  // 컴포넌트가 마운트될 때 실행되는 useEffect
  useEffect(() => {
    // 로그인 상태 확인
    const user = JSON.parse(localStorage.getItem("currentUser"));
    if (!user) {
      navigate("/login"); // 로그인되지 않았으면 로그인 페이지로 이동
      return;
    }
    setUserData(user); // 사용자 정보 상태에 저장

    // 예약 정보를 가져오는 비동기 함수
    const fetchReservationData = async () => {
      try {
        setIsLoading(true); // 로딩 상태 시작
        const jwtToken = localStorage.getItem("accessToken"); // JWT 토큰 가져오기
        const response = await fetchReservations(user.email, jwtToken); // 예약 정보 API 호출

        setReservations(response); // API 응답 데이터 상태에 저장 (response가 배열이라고 가정)
      } catch (err) {
        // 에러 발생 시 에러 메시지 설정
        setError(err.message || "예약 정보를 불러오는 데 실패했습니다.");
      } finally {
        setIsLoading(false); // 로딩 상태 종료
      }
    };

    fetchReservationData(); // 예약 정보 가져오기 함수 호출
  }, [navigate]); // navigate가 변경될 때마다 useEffect 실행

  // 예약 취소 버튼 클릭 시 실행되는 함수
  const handleDeleteReservation = async (reservationId) => {
    const isConfirmed = window.confirm("예약을 취소하시겠습니까?"); // 사용자에게 확인 요청
    if (isConfirmed) {
      try {
        const jwtToken = localStorage.getItem("accessToken"); // JWT 토큰 가져오기

        // 실제 API 호출 (예약 삭제)
        const response = await cancelReservation(reservationId, jwtToken);
        alert(response.message); // API 호출 성공 시 응답 메시지 출력

        // 예약 삭제 후 상태 업데이트
        const updatedReservations = reservations.filter(
          (reservation) => reservation.reservationId !== reservationId
        );
        setReservations(updatedReservations); // 예약 목록 상태 업데이트
      } catch (error) {
        console.error("예약 취소 실패:", error.message);
        alert(`예약 취소에 실패했습니다: ${error.message}`); // 에러 메시지 출력
      }
    }
  };

  // 날짜 및 시간을 포맷팅하는 함수
  const formatDateTime = (dateTimeStr) => {
    const date = new Date(dateTimeStr);
    if (isNaN(date)) return "잘못된 날짜";

    const options = {
      year: "numeric", // 연도 4자리 표시
      month: "2-digit", // 월 2자리 표시
      day: "2-digit", // 일 2자리 표시
      hour: "2-digit", // 시간 2자리 표시
      minute: "2-digit", // 분 2자리 표시
      hour12: false, // 24시간 형식
    };
    return new Intl.DateTimeFormat("ko-KR", options)
      .format(date)
      .replace(",", ""); // 포맷팅된 문자열에서 쉼표 제거
  };

  return (
    <div className="mypage-container">
      {/* 홈 버튼 */}
      <button className="home-button" onClick={() => navigate("/")}>
        🏠 홈
      </button>

      <h1>마이페이지</h1>

      {/* 사용자 정보 표시 */}
      {userData && (
        <div className="user-info">
          <h2>회원 정보</h2>
          <div className="info-item">
            <span>이메일:</span>
            <span>{userData.email}</span>
          </div>
          <div className="info-item">
            <span>이름:</span>
            <span>{userData.username}</span>
          </div>
        </div>
      )}

      {/* 예약 내역 표시 */}
      <div className="reservations">
        <h2>예약 내역</h2>
        {isLoading ? (
          // 로딩 중일 때 표시되는 내용
          <p>예약 정보를 불러오는 중...</p>
        ) : error ? (
          // 에러 발생 시 표시되는 내용
          <p className="error-message" style={{ color: "red" }}>{error}</p>
        ) : reservations.length > 0 ? (
          // 예약 내역이 있을 때 표시되는 내용
          <div className="reservation-list">
            {reservations.map((reservation) => (
              <div key={reservation.reservationId} className="reservation-item">
                <div className="reservation-info">
                  <h3>{reservation.restaurantName || "알 수 없는 식당"}</h3>
                  <p>날짜 및 시간: {formatDateTime(reservation.reservationDateTime)}</p>
                  <p>인원: {reservation.numberOfGuests}명</p>
                  <p>예약자 이메일: {reservation.userEmail}</p>
                </div>
                {/* 예약 취소 버튼 */}
                <button
                  className="delete-button"
                  onClick={() => handleDeleteReservation(reservation.reservationId)}
                >
                  예약 취소
                </button>
              </div>
            ))}
          </div>
        ) : (
          // 예약 내역이 없을 때 표시되는 내용
          <p className="no-reservations">예약 내역이 존재하지 않습니다.</p>
        )}
      </div>
    </div>
  );
};

export default MyPage;
