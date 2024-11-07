import React, { useState, useCallback } from "react";
import { Link, useNavigate } from "react-router-dom";
import { GoogleMap, LoadScript, Marker } from "@react-google-maps/api";
import SearchModal from "./SearchModal";
import RestaurantDetailPanel from "./RestaurantDetailPanel";
import "./Main.css";

const Main = () => {
  const [selectedRestaurantDetail, setSelectedRestaurantDetail] =
    useState(null);
  const [isSearchModalOpen, setIsSearchModalOpen] = useState(true);
  const [selectedRestaurant, setSelectedRestaurant] = useState(null);
  const [showReservationModal, setShowReservationModal] = useState(false);
  const [reservationData, setReservationData] = useState({
    date: "",
    time: "",
    people: 1,
  });
  const navigate = useNavigate();
  // 지도 설정
  // 로그인 상태 확인
  const isLoggedIn = localStorage.getItem("currentUser");
// 로그아웃 처리
  const handleLogout = () => {
    localStorage.removeItem("currentUser"); // 로그인 정보 삭제
    localStorage.removeItem("accessToken");
    setSelectedRestaurantDetail(null); // 열려있는 상세 정보/예약 패널 닫기
    setIsSearchModalOpen(true); // 검색 모달 다시 열기
    navigate("/"); // 메인 페이지로 이동 (새로고침 효과)
    window.location.reload(); // 페이지 새로고침하여 상태 초기화
  };

  const mapContainerStyle = {
    width: "100%",
    height: "calc(100vh - 60px)", // 네비게이션 바 높이만큼 제외
    position: "relative",
    marginTop: "60px", // 네비게이션 바 높이만큼 여백 추가
  };

  const center = {
    lat: 37.555946,
    lng: 126.937163,
  };

  const mapOptions = {
    zoom: 15,
    disableDefaultUI: false,
    zoomControl: true,
  }; // 식당 선택 처리 함수
  const handleSelectRestaurant = (restaurant) => {
    setSelectedRestaurant(restaurant);
    setSelectedRestaurantDetail(restaurant); // 상세 정보 패널 표시
  };

  // 예약 처리 함수
  const handleReservation = (e) => {
    e.preventDefault();
    console.log("예약 정보:", {
      restaurant: selectedRestaurant?.name,
      ...reservationData,
    });
    alert("예약이 완료되었습니다!");
    setShowReservationModal(false);
    setReservationData({ date: "", time: "", people: 1 });
  };

  return (
    <div className="main-container">
      <nav className="nav-bar">
        <div className="nav-center">
          <Link to="/" className="logo-link">
            <h1 className="logo">구구 식당 예약 사이트</h1>
          </Link>
        </div>
        <div className="nav-right">
          {!localStorage.getItem("currentUser") ? (
            // 비로그인 상태: 로그인, 회원가입 버튼 표시
            <>
              <Link to="/login" className="nav-button">
                로그인
              </Link>
              <Link to="/signup" className="nav-button">
                회원가입
              </Link>
            </>
          ) : (
            // 로그인 상태: 마이페이지, 로그아웃 버튼 표시
            <>
              <Link to="/mypage" className="nav-button">
                마이페이지
              </Link>
              <button
                onClick={handleLogout}
                className="nav-button logout-button"
              >
                로그아웃
              </button>
            </>
          )}
        </div>
      </nav>

      {/* 검색 모달 */}
      <SearchModal
        isOpen={isSearchModalOpen}
        onClose={() => setIsSearchModalOpen(false)}
        onSelectRestaurant={handleSelectRestaurant}
      />

      {/* 식당 상세 정보 패널 */}
      {selectedRestaurantDetail && (
        <RestaurantDetailPanel
          restaurant={selectedRestaurantDetail}
          onClose={() => setSelectedRestaurantDetail(null)}
        />
      )}

      {/* Google Maps */}
      <div className="map-container">
        <LoadScript
          googleMapsApiKey="AIzaSyAkPpqicbdu9yeB43AmjEuvaUjvLC2xKsM"
          libraries={["places"]}
        >
          <GoogleMap
            mapContainerStyle={mapContainerStyle}
            center={
              selectedRestaurant
                ? { lat: selectedRestaurant.lat, lng: selectedRestaurant.lng }
                : center
            }
            options={mapOptions}
          >
            {selectedRestaurant && (
              <Marker
                position={{
                  lat: selectedRestaurant.lat,
                  lng: selectedRestaurant.lng,
                }}
              />
            )}
          </GoogleMap>
        </LoadScript>
      </div>
    </div>
  );
};

export default Main;
