import React, { useState } from "react";
import "../css/Reservation.css";

const Reservation = () => {
  const [date, setDate] = useState("");
  const [time, setTime] = useState("");
  const [people, setPeople] = useState(1);
  const [currentStep, setCurrentStep] = useState(1);

  // 예약 가능한 시간대
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

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log("예약 정보:", { date, time, people });
    alert("예약이 완료되었습니다!");
  };

  const renderStep = () => {
    switch (currentStep) {
      case 1:
        return (
          <div className="step-container">
            <h3 className="step-title">날짜 선택</h3>
            <div className="date-input-container">
              <input
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                min={new Date().toISOString().split("T")[0]}
                className="date-input"
              />
            </div>
            <button
              className="next-button"
              onClick={() => setCurrentStep(2)}
              disabled={!date}
            >
              다음
            </button>
          </div>
        );

      case 2:
        return (
          <div className="step-container">
            <h3 className="step-title">시간 선택</h3>
            <div className="time-grid">
              {timeSlots.map((slot) => (
                <button
                  key={slot}
                  className={`time-button ${time === slot ? "selected" : ""}`}
                  onClick={() => setTime(slot)}
                >
                  {slot}
                </button>
              ))}
            </div>
            <div className="button-group">
              <button className="back-button" onClick={() => setCurrentStep(1)}>
                이전
              </button>
              <button
                className="next-button"
                onClick={() => setCurrentStep(3)}
                disabled={!time}
              >
                다음
              </button>
            </div>
          </div>
        );

      case 3:
        return (
          <div className="step-container">
            <h3 className="step-title">인원 선택</h3>
            <div className="people-counter">
              <button
                className="counter-button"
                onClick={() => setPeople((p) => Math.max(1, p - 1))}
              >
                -
              </button>
              <span className="people-count">{people}명</span>
              <button
                className="counter-button"
                onClick={() => setPeople((p) => Math.min(10, p + 1))}
              >
                +
              </button>
            </div>
            <div className="reservation-summary">
              <p className="summary-title">선택하신 예약 정보</p>
              <ul className="summary-list">
                <li>날짜: {date}</li>
                <li>시간: {time}</li>
                <li>인원: {people}명</li>
              </ul>
            </div>
            <div className="button-group">
              <button className="back-button" onClick={() => setCurrentStep(2)}>
                이전
              </button>
              <button className="submit-button" onClick={handleSubmit}>
                예약하기
              </button>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="reservation-container">
      <div className="reservation-card">
        <div className="card-header">
          <h2 className="card-title">예약하기</h2>
          <p className="card-description">
            원하시는 날짜와 시간을 선택해주세요
          </p>
        </div>
        <div className="card-content">{renderStep()}</div>
      </div>
    </div>
  );
};

export default Reservation;
