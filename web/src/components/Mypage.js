import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchReservations, cancelReservation } from "../api/reservation"; // API 준비 완료 후 활성화
import "./MyPage.css";

const MyPage = () => {
  const navigate = useNavigate();
  const [userData, setUserData] = useState(null);
  const [reservations, setReservations] = useState([]);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  // 식당 ID와 이름을 매핑
  const restaurantNames = {
    "123e4567-e89b-12d3-a456-426614174000": "신촌 파스타",
    "223e4567-e89b-12d3-a456-426614174000": "강남 스테이크 하우스",
    "123e4567-e89b-12d3-a456-426614174001": "홍대 초밥",
    // 추가 식당 매핑...
  };

  useEffect(() => {
    // 로그인 상태 확인
    const user = JSON.parse(localStorage.getItem("currentUser"));
    if (!user) {
      navigate("/login");
      return;
    }
    setUserData(user);

    // 예약 정보 확인 API 호출
    const fetchReservationData = async () => {
      try {
        setIsLoading(true);
        const jwtToken = localStorage.getItem("accessToken"); // JWT 토큰 가져오기
        const response = await fetchReservations(user.email, jwtToken); // API 호출

        console.log(response);
        setReservations(response); // API 응답 데이터 상태에 저장 (response가 배열이라고 가정)
      } catch (err) {
        setError(err.message || "예약 정보를 불러오는 데 실패했습니다."); // 에러 처리
      } finally {
        setIsLoading(false);
      }
    };

    fetchReservationData();
  }, [navigate]);

  // 예약 삭제 함수
  const handleDeleteReservation = async (reservationId) => {
    const isConfirmed = window.confirm("예약을 취소하시겠습니까?");
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
        setReservations(updatedReservations);
      } catch (error) {
        console.error("예약 취소 실패:", error.message);
        alert(`예약 취소에 실패했습니다: ${error.message}`);
      }
    }
  };

  // Function to format reservationDateTime
  const formatDateTime = (dateTimeStr) => {
    const date = new Date(dateTimeStr);
    if (isNaN(date)) return "잘못된 날짜";

    // Format to "YYYY-MM-DD HH:MM" in Korean locale
    const options = {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    };
    return new Intl.DateTimeFormat('ko-KR', options).format(date).replace(',', '');
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
                <span>{userData.username}</span>
              </div>
            </div>
        )}

        <div className="reservations">
          <h2>예약 내역</h2>
          {isLoading ? (
              <p>예약 정보를 불러오는 중...</p>
          ) : error ? (
              <p className="error-message" style={{ color: "red" }}>{error}</p>
          ) : reservations.length > 0 ? (
              <div className="reservation-list">
                {reservations.map((reservation) => (
                    <div key={reservation.reservationId} className="reservation-item">
                      <div className="reservation-info">
                        <h3>{restaurantNames[reservation.restaurantId] || "알 수 없는 식당"}</h3>
                        <p>날짜 및 시간: {formatDateTime(reservation.reservationDateTime)}</p>
                        <p>인원: {reservation.numberOfGuests}명</p>
                        <p>예약자 이메일: {reservation.userEmail}</p>
                      </div>
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
              <p className="no-reservations">예약 내역이 없습니다.</p>
          )}
        </div>
      </div>
  );
};

export default MyPage;