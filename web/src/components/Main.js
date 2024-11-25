// 필요한 모듈 및 컴포넌트 import 
import React, { useState, useCallback } from "react";
import { Link, useNavigate } from "react-router-dom";
import { GoogleMap, LoadScript, Marker } from "@react-google-maps/api";
import SearchModal from "./SearchModal";
import RestaurantDetailPanel from "./RestaurantDetailPanel";
import "./Main.css";
import logo from '../img/respa-kawaii-logo4.png'; // 9900팀 로고 

// 메인 컴포넌트 정의
const Main = () => {
  const [selectedRestaurantDetail, setSelectedRestaurantDetail] = useState(null); // 선택된 식당의 상세 정보
  const [isSearchModalOpen, setIsSearchModalOpen] = useState(true); // 검색 모달의 열림 상태
  const [selectedRestaurant, setSelectedRestaurant] = useState(null); // 선택된 식당
  const [showReservationModal, setShowReservationModal] = useState(false); // 예약 모달 표시 여부
  const [reservationData, setReservationData] = useState({
    date: "",
    time: "",
    people: 1,
  }); // 예약 정보 초기 상태
  
  const navigate = useNavigate(); // 페이지 이동을 위한 훅
  
  // 로그아웃 처리
  const handleLogout = () => {
    localStorage.removeItem("currentUser"); // 로컬 스토리지에서 사용자 정보 제거
    localStorage.removeItem("accessToken"); // 로컬 스토리지에서 엑세스 토큰 제거
    setSelectedRestaurantDetail(null); // 선택된 식당 상세 정보 초기화
    setIsSearchModalOpen(true); // 검색 모달을 열림 상태로 변경
    navigate("/"); // 홈페이지로 이동
    window.location.reload(); // 페이지 새로고침
  };

  // 구글 맵 컨테이너 스타일 설정
  const mapContainerStyle = {
    width: "100%", // 너비 100%
    height: "calc(100vh - 60px)", // 화면 높이에서 60px 뻰 값
    position: "relative", 
    marginTop: "60px", // 상단 마진 60px
  };

  // 지도 중심 좌표(신촌역2호선 좌표)
  const center = {
    lat: 37.555946, // 위도
    lng: 126.937163, // 경도
  };

  // 구글 맵 옵션 설정
  const mapOptions = { 
    zoom: 15, // 초기 줌  레벨
    disableDefaultUI: false, // 기본U UI 사용 여부
    zoomControl: true, // 줌 컨트롤 표시 여부
  };

  // 식당 선택 시 호출되는 함수
  const handleSelectRestaurant = (restaurant) => {
    setSelectedRestaurant(restaurant); // 선택된 식당 상태 업데이트
    setSelectedRestaurantDetail(restaurant); // 선택된 식당 상세 정보 업데이트
  };

  //예약 처리 함수
  const handleReservation = (e) => {
    e.preventDefault(); // 기본 이벤트 막기
    console.log("예약 정보:", {
      restaurant: selectedRestaurant?.name,
      ...reservationData,
    }); // 예약 정보 콘솔에 출력
    alert("예약이 완료되었습니다!"); // 예약 완료 알림
    setShowReservationModal(false); // 예약 모달 닫기
    setReservationData({ date: "", time: "", people: 1 }); // 예약 정보 초기화
  };
  
  
  // 로그인 상태 확인 (로컬 스토리지 사용)
  const isLoggedIn = localStorage.getItem("currentUser") || false;
  
  return (
      <div className="main-container">
        {/* 네비게이션 바 */}
        <nav className="nav-bar">
        {/* 로고 및 홈으로 이동하는 링크 */}
          <div className="nav-center">
            <Link to="/" className="nav-center">
              <img
                  src={logo}
                  alt="구구 식당 예약 사이트"
                  className="logo-image"
              />
            </Link>
          </div>
          {/* 네비게이션 바 우측 메뉴 */}
          <div className="nav-right">
            {!isLoggedIn ? (
                // 로그인하지 않은 경우
                <>
                  <Link to="/login" className="nav-button">
                    로그인
                  </Link>
                  <Link to="/signup" className="nav-button">
                    회원가입
                  </Link>
                </>
            ) : (
              // 로그인한 경우
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
        {/* 검색 모달 컴포넌트 */}
        <SearchModal
            isOpen={isSearchModalOpen} // 모달 열림 상태
            onClose={() => setIsSearchModalOpen(false)} // 모달 닫기 함수 
            onSelectRestaurant={handleSelectRestaurant} // 식당 선택시 호출 함수
        />
        {/* 선택된 식당 상세 정보 패널 */}
        {selectedRestaurantDetail && (
            <RestaurantDetailPanel
                restaurant={selectedRestaurantDetail} // 선택된 식당 정보 전달
                onClose={() => setSelectedRestaurantDetail(null)} // 패널 닫기 함수
            />
        )}
        {/* 구글 맵 표시 영역 */}
        <div className="map-container">
          <LoadScript
              googleMapsApiKey={process.env.REACT_APP_GOOGLE_MAP_KEY} // 구글 맵 API 키 
              libraries={["places"]} // 사용할 라이브러리
          >
            <GoogleMap
                mapContainerStyle={mapContainerStyle} // 지도 컨테이너 스타일
                center={
                  selectedRestaurantDetail
                      ? { lat: selectedRestaurantDetail.latitude, lng: selectedRestaurantDetail.longitude }
                      : center // 기본 중심 위치
                }
                options={mapOptions} // 지도 옵션
            >
              {/* 선택된 식당이 있을 경우 마커 표시 */}
              {selectedRestaurantDetail && (
                  <Marker
                      position={{
                        lat: selectedRestaurantDetail.latitude,
                        lng: selectedRestaurantDetail.longitude,
                      }} // 마커 위치 설정
                  />
              )}
            </GoogleMap>
          </LoadScript>
        </div>
      </div>
  );
};
export default Main;
