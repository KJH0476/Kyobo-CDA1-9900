import React, { useState, useCallback } from "react";
import { Link, useNavigate } from "react-router-dom";
import { GoogleMap, LoadScript, Marker } from "@react-google-maps/api";
import SearchModal from "./SearchModal";
import RestaurantDetailPanel from "./RestaurantDetailPanel";
import "./Main.css";

const Main = () => {
  const [selectedRestaurantDetail, setSelectedRestaurantDetail] = useState(null);
  const [isSearchModalOpen, setIsSearchModalOpen] = useState(true);
  const [selectedRestaurant, setSelectedRestaurant] = useState(null);
  const [showReservationModal, setShowReservationModal] = useState(false);
  const [reservationData, setReservationData] = useState({
    date: "",
    time: "",
    people: 1,
  });
  const navigate = useNavigate();

  // 로그아웃 처리
  const handleLogout = () => {
    localStorage.removeItem("currentUser");
    localStorage.removeItem("accessToken");
    setSelectedRestaurantDetail(null);
    setIsSearchModalOpen(true);
    navigate("/");
    window.location.reload();
  };

  const mapContainerStyle = {
    width: "100%",
    height: "calc(100vh - 60px)",
    position: "relative",
    marginTop: "60px",
  };

  const center = {
    lat: 37.555946,
    lng: 126.937163,
  };

  const mapOptions = {
    zoom: 15,
    disableDefaultUI: false,
    zoomControl: true,
  };

  const handleSelectRestaurant = (restaurant) => {
    setSelectedRestaurant(restaurant);
    setSelectedRestaurantDetail(restaurant);
  };

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

  // 현재 로그인 상태 확인 (세션스토리지 사용)
  const isLoggedIn = localStorage.getItem("currentUser") || false;

  return (
    <div className="main-container">
      <nav className="nav-bar">
        <div className="nav-center">
          <Link to="/" className="logo-link">
            <h1 className="logo">구구 식당 예약 사이트</h1>
          </Link>
        </div>
        <div className="nav-right">
          {!isLoggedIn ? (
            <>
              <Link to="/login" className="nav-button">
                로그인
              </Link>
              <Link to="/signup" className="nav-button">
                회원가입
              </Link>
            </>
          ) : (
            <>
              <Link to="/mypage" className="nav-button">
                마이페이지
              </Link>
              <button onClick={handleLogout} className="nav-button logout-button">
                로그아웃
              </button>
            </>
          )}
        </div>
      </nav>

      <SearchModal
        isOpen={isSearchModalOpen}
        onClose={() => setIsSearchModalOpen(false)}
        onSelectRestaurant={handleSelectRestaurant}
      />

      {selectedRestaurantDetail && (
        <RestaurantDetailPanel
          restaurant={selectedRestaurantDetail}
          onClose={() => setSelectedRestaurantDetail(null)}
        />
      )}

      <div className="map-container">
        <LoadScript
          googleMapsApiKey={process.env.REACT_APP_GOOGLE_MAP_KEY}
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