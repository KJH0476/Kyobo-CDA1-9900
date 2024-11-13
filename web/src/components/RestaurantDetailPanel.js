import React, { useState, useEffect } from "react";
import "./RestaurantDetailPanel.css";
//import { createReservation, registerWaitlist, getAvailabilityTimeSlots } from "../api/reservation"; // 경로는 실제 파일 구조에 맞게 조정

const RestaurantDetailPanel = ({ restaurant, onClose }) => {
  const [showReservation, setShowReservation] = useState(false);
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedTime, setSelectedTime] = useState("");
  const [peopleCount, setPeopleCount] = useState(1);
  const [bookedTimeSlots, setBookedTimeSlots] = useState(new Set());
  const [isWaitlist, setIsWaitlist] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

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
    "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00",
    "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
  ];

  useEffect(() => {
    if (selectedDate) {
      const reservations = JSON.parse(localStorage.getItem("reservations") || "[]");
      const waitlist = JSON.parse(localStorage.getItem("waitlist") || "[]");
      const bookedTimes = new Set(
        reservations
          .filter(r => r.restaurantId === restaurant.id && 
                      r.reservationDate === selectedDate)
          .map(r => r.reservationTime)
      );
      setBookedTimeSlots(bookedTimes);
      setIsWaitlist(bookedTimes.has(selectedTime));
    }
  }, [selectedDate, selectedTime, restaurant.id]);

  const handlePeopleChange = (increment) => {
    const newCount = peopleCount + increment;
    if (newCount >= 1 && newCount <= 10) {
      setPeopleCount(newCount);
    }
  };


  
  const handleSubmitReservation = async () => {
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
      restaurantId: restaurant.id,
      userEmail: currentUser.email,
      reservationDate: selectedDate,
      reservationTime: selectedTime,
      numberOfGuests: peopleCount,
      restaurantName: restaurant.name
    };

    try {

      setIsLoading(true);

      // 예약 가능 시간대 API 호출
     /*
      const availabilityResponse = await getAvailabilityTimeSlots(
        restaurant.id,
        jwtToken,
        currentUser.email
      );

      if (availabilityResponse.statusCode === 200) {
        const availableTimes = availabilityResponse.availabilityTimeList;
        const isAvailable = availableTimes.some(
          (timeSlot) =>
            timeSlot.reservationDate === selectedDate &&
            timeSlot.reservationTime === selectedTime &&
            timeSlot.availableTables > 0
        );

        if (!isAvailable) {
          alert("예약 가능 시간대 조회에 실패했습니다.");
          return;
        }
      } else {
        alert("예약 가능 시간대 조회에 실패했습니다.");
        return;
      }
      */


      // 백엔드 API 호출 부분 주석 처리 
      // 예약 생성 API 호출 o 부분 주석 해제
      /*
      const response = await createReservation(reservationData, jwtToken);

    if (response.statusCode === 201) {
      // 예약 생성 성공
      const reservationDetails = response.reservationDto; // reservationDto 데이터 접근
      alert(
        `예약이 완료되었습니다!\n예약 ID: ${reservationDetails.reservationId}\n식당: ${reservationDetails.restaurantId}\n날짜 및 시간: ${reservationDetails.reservationDateTime}\n인원: ${reservationDetails.numberOfGuests}`
      );
    } else if (response.status === "waiting") {
      // 대기 목록 등록
      alert(`대기 목록에 등록되었습니다. 대기 순번: ${response.position}`);
    }

  onClose(); // 예약 완료 후 화면 닫기
} catch (error) {
  console.error("예약 생성 실패:", error);
  alert("예약 생성에 실패했습니다. 다시 시도해주세요.");
}

      // 예약 대기 등록 API 호출 부분 주석 처리
      /*
      const jwtToken = currentUser.jwtToken; // JWT 토큰이 있는 경우

      const waitlistData = {
        restaurantId: restaurant.id,
        userEmail: currentUser.email,
        reservationDate: selectedDate,
        reservationTime: selectedTime,
        numberOfGuests: peopleCount
      };

      // 예약 대기 API 호출 o
      const waitlistResponse = await registerWaitlist(waitlistData, jwtToken);

      if (waitlistResponse.statusCode === 201) {
        const waitListDetails = waitlistResponse.waitListDto;

         alert(
            `예약 대기가 등록되었습니다!\n` +
            `대기 ID: ${waitListDetails.waitListId}\n` +
            `식당: ${waitListDetails.restaurantName}\n` +
            `예약 날짜 및 시간: ${waitListDetails.reservationDateTime}`
          );
       } else {
         alert("예약 대기 등록에 실패했습니다.");
       }

      onClose();
      */

      // API 호출 시 아래 LocalStorage 필요없음
      // localStorage를 이용한 예약 처리 로직

      // 이미 예약된 시간인지 확인
      const isTimeSlotBooked = bookedTimeSlots.has(selectedTime);
      
      if (isTimeSlotBooked) {
        // 대기 리스트로 처리
        handleWaitlist(reservationData);
      } else {
        // 일반 예약 처리
        handleNormalReservation(reservationData);
      }
    } catch (error) {
      console.error("예약 처리 중 오류:", error);
      alert("예약 처리 중 문제가 발생했습니다. 다시 시도해주세요.");
    }
  };

  const handleWaitlist = (reservationData) => {
    const waitlist = JSON.parse(localStorage.getItem("waitlist") || "[]");
    
    // 대기 목록에 이미 있는지 확인
    const isAlreadyWaitlisted = waitlist.some(
      w => w.userEmail === reservationData.userEmail && 
          w.restaurantId === reservationData.restaurantId && 
          w.reservationDate === reservationData.reservationDate && 
          w.reservationTime === reservationData.reservationTime
    );

    if (isAlreadyWaitlisted) {
      alert("이미 대기 목록에 등록되어 있습니다.");
      return;
    }

    // 대기 정보 추가
    const waitlistEntry = {
      ...reservationData,
      status: 'waiting',
      position: waitlist.length + 1,
      registeredAt: new Date().toISOString()
    };
    
    waitlist.push(waitlistEntry);
    localStorage.setItem("waitlist", JSON.stringify(waitlist));
    
    alert(`대기 목록에 등록되었습니다. 대기 순번: ${waitlist.length}`);
    onClose();
  };

  const handleNormalReservation = (reservationData) => {
    const reservations = JSON.parse(localStorage.getItem("reservations") || "[]");
    
    // 다시 한번 예약 가능 여부 확인
    const isTimeSlotBooked = reservations.some(
      r => r.restaurantId === reservationData.restaurantId && 
          r.reservationDate === reservationData.reservationDate && 
          r.reservationTime === reservationData.reservationTime
    );

    if (isTimeSlotBooked) {
      // 이미 예약되었다면 대기 리스트로 전환
      handleWaitlist(reservationData);
      return;
    }

    // 예약 정보 추가
    const reservationEntry = {
      ...reservationData,
      status: 'confirmed',
      createdAt: new Date().toISOString()
    };
    
    reservations.push(reservationEntry);
    localStorage.setItem("reservations", JSON.stringify(reservations));
    setBookedTimeSlots(prev => new Set([...prev, reservationData.reservationTime]));
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