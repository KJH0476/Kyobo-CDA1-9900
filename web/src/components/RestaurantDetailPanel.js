import React, { useState } from "react";
import "./RestaurantDetailPanel.css";

const RestaurantDetailPanel = ({ restaurant, onClose }) => {
  const [showReservation, setShowReservation] = useState(false);
  // 예약 정보 상태 관리
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedTime, setSelectedTime] = useState("");
  const [peopleCount, setPeopleCount] = useState(1);

  // 메뉴 데이터 (예시)
  const menus = {
    "신촌 파스타": [
      {
        name: "까르보나라",
        price: "15,000원",
        description: "생크림 베이스의 깊은 맛",
      },
      // ... 기존 메뉴 데이터
    ],
    // ... 다른 식당 메뉴
  };

  const restaurantMenus = menus[restaurant.name] || [];

  // 예약 가능 시간대
  const timeSlots = [
    "11:00",
    "11:30",
    "12:00",
    "12:30",
    "13:00",
    "13:30",
    "14:00",
    "14:30",
    "15:00",
    "15:30",
    "16:00",
    "16:30",
    "17:00",
    "17:30",
    "18:00",
    "18:30",
    "19:00",
    "19:30",
  ];

  // 인원 수 변경 함수
  const handlePeopleChange = (increment) => {
    const newCount = peopleCount + increment;
    if (newCount >= 1 && newCount <= 10) {
      setPeopleCount(newCount);
    }
  };

  // 예약 제출 처리
  const handleSubmitReservation = () => {
    if (!selectedDate || !selectedTime) {
      alert("날짜와 시간을 선택해주세요.");
      return;
    }

    // 현재 로그인한 사용자 정보 가져오기
    const currentUser = JSON.parse(localStorage.getItem("currentUser"));

    if (!currentUser) {
      alert("로그인이 필요한 서비스입니다.");
      return;
    }

    const reservationData = {
      userId: currentUser.id,
      restaurant: restaurant.name,
      date: selectedDate,
      time: selectedTime,
      people: peopleCount,
      createdAt: new Date().toISOString(),
    };

    // 예약 정보 저장
    const saveReservation = (reservationData) => {
      const reservations = JSON.parse(
        localStorage.getItem("reservations") || "[]"
      );
      reservations.push(reservationData);
      localStorage.setItem("reservations", JSON.stringify(reservations));
    };

    // 예약 저장 실행
    saveReservation(reservationData);

    alert("예약이 완료되었습니다!");
    onClose();
  };

  const ReservationForm = () => (
    <div className="reservation-form">
      <h3>예약하기</h3>

      {/* 날짜 선택 */}
      <div className="date-selection">
        <label>날짜 선택</label>
        <input
          type="date"
          className={`date-input ${selectedDate ? "selected" : ""}`}
          value={selectedDate}
          onChange={(e) => setSelectedDate(e.target.value)}
          min={new Date().toISOString().split("T")[0]}
        />
      </div>

      {/* 시간 선택 */}
      <div className="time-selection">
        <label>시간 선택</label>
        <div className="time-grid">
          {timeSlots.map((time) => (
            <button
              key={time}
              className={`time-slot ${selectedTime === time ? "selected" : ""}`}
              onClick={() => setSelectedTime(time)}
            >
              {time}
            </button>
          ))}
        </div>
      </div>

      {/* 인원 선택 */}
      <div className="people-selection">
        <label>인원 선택</label>
        <div className="people-counter">
          <button
            className="counter-btn"
            onClick={() => handlePeopleChange(-1)}
            disabled={peopleCount <= 1}
          >
            -
          </button>
          <span className="people-count">{peopleCount}명</span>
          <button
            className="counter-btn"
            onClick={() => handlePeopleChange(1)}
            disabled={peopleCount >= 10}
          >
            +
          </button>
        </div>
      </div>

      {/* 예약 정보 요약 */}
      {(selectedDate || selectedTime || peopleCount > 1) && (
        <div className="reservation-summary">
          <h4>예약 정보</h4>
          <div className="summary-content">
            <div className="summary-item">
              <span>날짜:</span>
              <span>{selectedDate || "날짜를 선택해주세요"}</span>
            </div>
            <div className="summary-item">
              <span>시간:</span>
              <span>{selectedTime || "시간을 선택해주세요"}</span>
            </div>
            <div className="summary-item">
              <span>인원:</span>
              <span>{peopleCount}명</span>
            </div>
          </div>
        </div>
      )}

      {/* 예약 버튼 */}
      <button
        className={`submit-reservation ${
          !selectedDate || !selectedTime ? "disabled" : ""
        }`}
        onClick={handleSubmitReservation}
        disabled={!selectedDate || !selectedTime}
      >
        예약 확정하기
      </button>
    </div>
  );

  const RestaurantInfo = () => (
    <div className="restaurant-info-container">
      <div className="restaurant-header">
        <h2>{restaurant.name}</h2>
        <div className="restaurant-meta">
          <span>⭐ {restaurant.rating}</span>
          <span>리뷰 {restaurant.reviewCount}</span>
          <span>{restaurant.type}</span>
        </div>
        <p className="restaurant-address">{restaurant.address}</p>
      </div>

      <div className="menu-section">
        <h3>메뉴</h3>
        <div className="menu-list">
          {restaurantMenus.map((menu, index) => (
            <div key={index} className="menu-item">
              <div className="menu-info">
                <h4>{menu.name}</h4>
                <p className="menu-description">{menu.description}</p>
              </div>
              <div className="menu-price">{menu.price}</div>
            </div>
          ))}
        </div>
      </div>

      <button
        className="reservation-button"
        onClick={() => setShowReservation(true)}
      >
        예약하기
      </button>
    </div>
  );

  return (
    <div className="detail-panel">
      <button className="close-button" onClick={onClose}>
        &times;
      </button>
      {showReservation ? <ReservationForm /> : <RestaurantInfo />}
    </div>
  );
};

export default RestaurantDetailPanel;
