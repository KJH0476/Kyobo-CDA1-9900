import React, { useState, useEffect } from "react";
import "./RestaurantDetailPanel.css";

const RestaurantDetailPanel = ({ restaurant, onClose }) => {
  const [showReservation, setShowReservation] = useState(false);
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedTime, setSelectedTime] = useState("");
  const [peopleCount, setPeopleCount] = useState(1);
  const [bookedTimeSlots, setBookedTimeSlots] = useState(new Set());
  const [isWaitlist, setIsWaitlist] = useState(false);

  const menus = {
    "신촌 파스타": [
      {
        name: "까르보나라",
        price: "15,000원",
        description: "생크림 베이스의 깊은 맛",
      },
    ],
  };

  const restaurantMenus = menus[restaurant.name] || [];

  const timeSlots = [
    "11:00", "11:30", "12:00", "12:30", "13:00", "13:30",
 
  ];

  useEffect(() => {
    if (selectedDate) {
      const reservations = JSON.parse(localStorage.getItem("reservations") || "[]");
      const waitlist = JSON.parse(localStorage.getItem("waitlist") || "[]");
      
      const bookedTimes = new Set(
        reservations
          .filter(r => r.restaurant === restaurant.name && r.date === selectedDate)
          .map(r => r.time)
      );
      setBookedTimeSlots(bookedTimes);

      // 선택된 시간이 예약됐는지 확인하고 waitlist 상태 업데이트
      setIsWaitlist(bookedTimes.has(selectedTime));
    }
  }, [selectedDate, selectedTime, restaurant.name]);

  const handlePeopleChange = (increment) => {
    const newCount = peopleCount + increment;
    if (newCount >= 1 && newCount <= 10) {
      setPeopleCount(newCount);
    }
  };

  const handleSubmitReservation = () => {
    if (!selectedDate || !selectedTime) {
      alert("날짜와 시간을 선택해주세요.");
      return;
    }

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

    if (isWaitlist) {
      handleWaitlist(reservationData);
    } else {
      handleNormalReservation(reservationData);
    }
  };

  const handleWaitlist = (reservationData) => {
    const waitlist = JSON.parse(localStorage.getItem("waitlist") || "[]");
    
    // 대기 목록에 이미 있는지 확인
    const isAlreadyWaitlisted = waitlist.some(
      w => w.userId === reservationData.userId && 
          w.restaurant === reservationData.restaurant && 
          w.date === reservationData.date && 
          w.time === reservationData.time
    );

    if (isAlreadyWaitlisted) {
      alert("이미 대기 목록에 등록되어 있습니다.");
      return;
    }

    waitlist.push({
      ...reservationData,
      status: 'waiting',
      position: waitlist.length + 1
    });
    
    localStorage.setItem("waitlist", JSON.stringify(waitlist));
    alert(`대기 목록에 등록되었습니다. 대기 순번: ${waitlist.length}`);
    onClose();
  };

  const handleNormalReservation = (reservationData) => {
    const reservations = JSON.parse(localStorage.getItem("reservations") || "[]");
    
    const isTimeSlotBooked = reservations.some(
      r => r.restaurant === restaurant.name && 
          r.date === selectedDate && 
          r.time === selectedTime
    );

    if (isTimeSlotBooked) {
      setIsWaitlist(true);
      handleWaitlist(reservationData);
      return;
    }

    reservations.push(reservationData);
    localStorage.setItem("reservations", JSON.stringify(reservations));
    setBookedTimeSlots(prev => new Set([...prev, selectedTime]));
    alert("예약이 완료되었습니다!");
    onClose();
  };

  const ReservationForm = () => (
    <div className="reservation-form">
      <h3>예약하기</h3>

      <div className="date-selection">
        <label>날짜 선택</label>
        <input
          type="date"
          className={`date-input ${selectedDate ? "selected" : ""}`}
          value={selectedDate}
          onChange={(e) => {
            setSelectedDate(e.target.value);
            setSelectedTime("");
            setIsWaitlist(false);
          }}
          min={new Date().toISOString().split("T")[0]}
        />
      </div>

      <div className="time-selection">
        <label>시간 선택</label>
        <div className="time-grid">
          {timeSlots.map((time) => {
            const isBooked = bookedTimeSlots.has(time);
            return (
              <button
                key={time}
                className={`time-slot ${selectedTime === time ? "selected" : ""} 
                          ${isBooked ? "booked" : ""}`}
                onClick={() => {
                  setSelectedTime(time);
                  setIsWaitlist(isBooked);
                }}
              >
                {time}
                {isBooked && <span className="booked-label"></span>}
              </button>
            );
          })}
        </div>
      </div>

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
            {isWaitlist && selectedTime && (
              <div className="waitlist-notice">
                ⚠️ 선택하신 시간은 예약이 마감되어 대기 등록만 가능합니다.
              </div>
            )}
          </div>
        </div>
      )}

      <button
        className={`submit-reservation ${!selectedDate || !selectedTime ? "disabled" : ""} 
                  ${isWaitlist ? "waitlist-button" : ""}`}
        onClick={handleSubmitReservation}
        disabled={!selectedDate || !selectedTime}
      >
        {isWaitlist ? "예약 대기하기" : "예약 확정하기"}
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