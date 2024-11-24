import React, { useState, useEffect } from "react";
import "./RestaurantDetailPanel.css";
import { createReservation, registerWaitlist, getAvailabilityTimeSlots } from "../api/reservation";

// RestaurantDetailPanel 컴포넌트
const RestaurantDetailPanel = ({ restaurant, onClose }) => {
  // 상태 관리
  const [showReservation, setShowReservation] = useState(false); // 예약 폼 표시 여부
  const [selectedDate, setSelectedDate] = useState(""); // 선택한 날짜
  const [selectedTime, setSelectedTime] = useState(""); // 선택한 시간
  const [peopleCount, setPeopleCount] = useState(1); // 예약 인원 수
  const [availabilityTimeList, setAvailabilityTimeList] = useState([]); // 예약 가능 시간대 리스트
  const [isWaitlist, setIsWaitlist] = useState(false); // 대기 등록 여부
  const [isLoading, setIsLoading] = useState(false); // 로딩 상태
  const [fetchError, setFetchError] = useState(""); // 에러 메시지


  const imageBaseUrl = process.env.REACT_APP_IMAGE_BASE_URL; // 메뉴 이미지의 기본 경로

  // 예약 가능한 시간대를 서버에서 가져오는 useEffect
  useEffect(() => {
    const fetchAvailability = async () => {
      if (!selectedDate) {
        setAvailabilityTimeList([]);
        return;
      }
  
      const jwtToken = localStorage.getItem("accessToken"); // JWT 토큰
      const currentUser = JSON.parse(localStorage.getItem("currentUser")); // 현재 사용자 정
  
      if (!currentUser) {
        setFetchError("로그인이 필요한 서비스입니다."); // 로그인 필요 메시지
        return;
      }
  
      try {
        setIsLoading(true); // 로딩 시작
        setFetchError(""); // 에러 초기화 
        // API 호출
        const response = await getAvailabilityTimeSlots(
          restaurant.id,
          jwtToken,
          currentUser.email,
          selectedDate
        );
  
        if (response.statusCode === 200) {
          // 날짜에 맞는 시간대 필터링
          const filteredAvailability = response.availabilityTimeList.filter(
            (slot) => new Date(slot.reservationDate).toISOString().split("T")[0] === selectedDate
          );
          // 시간대를 보기 좋게 포맷팅
          const formattedAvailability = filteredAvailability.map(slot => ({
            ...slot,
            reservationTime: slot.reservationTime.slice(0, 5)  // HH:MM:SS -> HH:MM
          }));
  
          setAvailabilityTimeList(formattedAvailability);// 시간대 리스트 상태 업데이트
        } else {
          setAvailabilityTimeList([]); // 시간대 초기화
          setFetchError("예약 가능 시간대 조회에 실패했습니다."); // 에러 메시지 설정
        }
      } catch (error) {
        console.error("예약 가능 시간대 조회 실패:", error); // 에러 로그 출력
        setFetchError("예약 가능 시간대 조회에 실패했습니다."); // 에러 메시지 설정
      } finally {
        setIsLoading(false); // 로딩 종료
      }
    };
  
    fetchAvailability();
  }, [selectedDate, restaurant.id]);
  

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

    const jwtToken = localStorage.getItem("accessToken");
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

      // 예약 가능 시간대 다시 조회 (최신 상태 확인)
      const availabilityResponse = await getAvailabilityTimeSlots(
          restaurant.id,
          jwtToken,
          currentUser.email,
          selectedDate
      );

      if (availabilityResponse.statusCode === 200) {
        // 선택한 날짜에 해당하는 시간대만 필터링
        const filteredAvailability = availabilityResponse.availabilityTimeList.filter(
            (slot) => slot.reservationDate === selectedDate
        );

        // reservationTime 포맷 변경 (HH:MM:SS -> HH:MM)
        const availableTimes = filteredAvailability.map(slot => ({
          ...slot,
          reservationTime: slot.reservationTime.slice(0, 5)
        }));

        const selectedTimeSlot = availableTimes.find(
            (slot) => slot.reservationTime === selectedTime
        );

        if (selectedTimeSlot && selectedTimeSlot.availableTables > 0) {
          // 예약 가능
          const response = await createReservation(reservationData, jwtToken);

          if (response.statusCode === 201) {
            const reservationDetails = response.reservationDto;
            alert(
                `예약이 완료되었습니다!\n예약 ID: ${reservationDetails.reservationId}\n식당: ${reservationDetails.restaurantName}\n날짜 및 시간: ${formatDateTime(reservationDetails.reservationDateTime)}\n인원: ${reservationDetails.numberOfGuests}`
            );
          } else if (response.status === 400) {
            alert(response.message);
          } else {
            alert("예약 생성에 실패했습니다.");
          }
        } else {
          // 예약 불가능, 대기 리스트 등록
          const waitlistResponse = await registerWaitlist(reservationData, jwtToken);

          if (waitlistResponse.statusCode === 201) {
            const waitListDetails = waitlistResponse.waitListDto;

            alert(
                `예약 대기가 등록되었습니다!\n` +
                `대기 ID: ${waitListDetails.waitListId}\n` +
                `식당: ${waitListDetails.restaurantName}\n` +
                `예약 날짜 및 시간: ${formatDateTime(waitListDetails.reservationDateTime)}`
            );
          } else {
            alert("예약 대기 등록에 실패했습니다.");
          }
        }
      } else {
        alert("예약 가능 시간대 조회에 실패했습니다.");
      }

      onClose(); // 예약 완료 후 화면 닫기
    } catch (error) {
      console.error("예약 생성 실패:", error);
      alert("이미 해당 시간에 예약이 존재합니다.");
    } finally {
      setIsLoading(false);
    }
  };

  // 날짜 및 시간을 보기 좋게 포맷팅하는 함수
  const formatDateTime = (dateTimeStr) => {
    const date = new Date(dateTimeStr);
    if (isNaN(date)) return "잘못된 날짜";

     // 한국 로케일로 "YYYY-MM-DD HH:MM" 형식으로 포맷팅
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
      {isLoading && <div className="loading">예약 가능 시간대 조회 중...</div>}
      {fetchError && <div className="error-message">{fetchError}</div>}
      {selectedDate && !isLoading && !fetchError && (
        <div className="time-selection">
          <label>시간 선택</label>
          <div className="time-grid">
            {availabilityTimeList.length > 0 ? (
              availabilityTimeList.map((slot, index) => (
                <button
                  key={index}
                  className={`time-slot ${selectedTime === slot.reservationTime ? "selected" : ""} 
                      ${slot.isReserved ? "reserved" : ""}
                      ${slot.availableTables === 0 ? "booked" : ""}`}
                  onClick={() => {
                    setSelectedTime(slot.reservationTime);
                    setIsWaitlist(slot.isReserved || slot.availableTables === 0);
                  }}
                >
                  {slot.reservationTime}
                  {slot.isReserved && <span className="reserved-label">예약됨</span>}
                  {slot.availableTables === 0 && !slot.isReserved && (
                    <span className="booked-label"></span>
                  )}
                </button>
              ))
            ) : (
              <div className="no-available-times" style={{ color: "red" }}>
                예약 가능한 시간이 없습니다.
              </div>
            )}
          </div>
        </div>
      )}
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
              <div className="waitlist-notice">⚠️ 선택하신 시간은 예약이 마감되어 대기 등록만 가능합니다.</div>
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
      {/* 식당 기본 정보 */}
      <div className="restaurant-header">
        <h2>{restaurant.restaurant_name}</h2>
        <p className="restaurant-address">주소: {restaurant.address}</p>
        <p className="restaurant-phone">전화번호: {restaurant.phone_number}</p>
        <p className="restaurant-type">
          종류: {restaurant.food_type?.join(", ")}
        </p>
      </div>

      {/* 메뉴 섹션 */}
      <div className="menu-section">
        <h3>메뉴</h3>
        <div className="menu-list">
          {restaurant.menu.map((menu, index) => (
            <div key={index} className="menu-item">
              <div className="menu-info">
                <h4>{menu.menu_name}</h4>
                <p className="menu-price">{menu.menu_price}원</p>
              </div>
              
              {menu.image_url && (  
                <img
                 // src={`${imageBaseUrl}/${menu.image_url}`}
                 src={`${imageBaseUrl}/${menu.image_url}`}
                 alt={menu.menu_name}
                 className="menu-image"
                />
              )}
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
