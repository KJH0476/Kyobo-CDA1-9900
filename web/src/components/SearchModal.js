import React, { useState } from "react";
import "./SearchModal.css";

const SearchModal = ({ isOpen, onClose, onSelectRestaurant, userEmail }) => {
  // 상태 관리
  const [searchTerm, setSearchTerm] = useState(""); // 식당 이름 검색어
  const [locationTerm, setLocationTerm] = useState(""); // 지역 검색어
  const [selectedCategories, setSelectedCategories] = useState([]); // 선택된 카테고리 배열
  const [filteredRestaurants, setFilteredRestaurants] = useState([]); // 검색 결과로 반환된 식당 데이터
  const [loading, setLoading] = useState(false); // 검색 요청 중 로딩 상태
  const [error, setError] = useState(null); // 에러 메시지 상태
  const [hasMore, setHasMore] = useState(true); // 더 보기 버튼 활성화를 위한 상태

  const BASE_URL = process.env.REACT_APP_BASE_URL; // 환경 변수에서 API 베이스 URL 가져오기

  // 검색 버튼 클릭 시 실행되는 함수
  const handleSearch = async () => {
    setLoading(true); // 검색 중 로딩 상태 활성화
    setError(null); // 에러 메시지 초기화
    try {
      // 검색 쿼리 파라미터 생성
      const queryParams = new URLSearchParams({
        restaurant_name: searchTerm || "", // 식당 이름 검색어
        address: locationTerm || "", // 지역 검색어
        food_type: selectedCategories.length > 0 ? selectedCategories.join(",") : "", // 선택된 카테고리
        limit: 10, // 한 번에 불러올 데이터 제한 (페이징 처리 가능)
      }).toString();

      // API 호출
      const response = await fetch(`${BASE_URL}/search/restaurants?${queryParams}`, {
        method: "GET", // GET 요청
        headers: {
          "Content-Type": "application/json",
          "X-User-Email": userEmail, // 사용자 이메일을 헤더에 추가
        },
      });

      if (!response.ok) {
        throw new Error("검색에 실패했습니다."); // HTTP 상태 코드가 200이 아니면 에러 처리
      }

      const data = await response.json(); // JSON 데이터 파싱

      // 검색 결과 처리
      if (data.statusCode === 200) {
        // 데이터의 길이가 limit보다 적으면 더 이상 데이터가 없음을 표시
        if (data.restaurants.length < 10) {
          setHasMore(false);
        }
        setFilteredRestaurants(data.restaurants); // 검색 결과를 상태에 저장
      } else {
        setError(data.message || "검색에 실패했습니다."); // 서버에서 반환된 에러 메시지 표시
      }
    } catch (err) {
      console.error("검색 중 오류 발생:", err);
      setError("검색에 실패했습니다. 다시 시도해주세요."); // 네트워크 또는 기타 에러 처리
    } finally {
      setLoading(false); // 로딩 상태 해제
    }
  };

  // 카테고리 선택 처리 함수
  const handleCategoryClick = (category) => {
    if (selectedCategories.includes(category)) {
      // 이미 선택된 카테고리를 클릭한 경우 해제
      setSelectedCategories(selectedCategories.filter((c) => c !== category));
    } else {
      // 새로운 카테고리를 추가
      setSelectedCategories([...selectedCategories, category]);
    }
  };

  if (!isOpen) return null; // 모달이 닫힌 경우 렌더링하지 않음

  return (
    <div className="search-modal-left">
      <div className="search-modal-content">
        {/* 식당 이름 검색 입력 */}
        <div className="search-input-container">
          <span className="search-icon">🔍</span>
          <input
            type="text"
            placeholder="식당 이름을 입력하세요"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)} // 검색어 상태 업데이트
            className="search-input"
          />
        </div>

        {/* 지역 검색 입력 */}
        <div className="location-input-container">
          <span className="search-icon">🔍</span>
          <input
            type="text"
            placeholder="지역 이름을 입력하세요 (예: 강남, 홍대)"
            value={locationTerm}
            onChange={(e) => setLocationTerm(e.target.value)} // 지역 상태 업데이트
            className="search-input"
          />
        </div>

        {/* 카테고리 선택 버튼 */}
        <div className="category-buttons">
          <button
            className={`category-btn ${selectedCategories.includes("한식") ? "active" : ""}`}
            onClick={() => handleCategoryClick("한식")}
          >
            <span className="category-icon">🍚</span> 한식
          </button>
          <button
            className={`category-btn ${selectedCategories.includes("중식") ? "active" : ""}`}
            onClick={() => handleCategoryClick("중식")}
          >
            <span className="category-icon">🥢</span> 중식
          </button>
          <button
            className={`category-btn ${selectedCategories.includes("양식") ? "active" : ""}`}
            onClick={() => handleCategoryClick("양식")}
          >
            <span className="category-icon">🍝</span> 양식
          </button>
        </div>

        {/* 검색 버튼 */}
        <div className="search-button-container">
          <button className="search-button" onClick={handleSearch}>
            검색
          </button>
        </div>

        {/* 로딩 메시지 */}
        {loading && <p className="loading-message">검색 중입니다...</p>}

        {/* 에러 메시지 */}
        {error && <p className="error-message">{error}</p>}

        {/* 검색 결과 출력 */}
        <div className="search-results">
          {filteredRestaurants.length > 0 ? (
            <>
              {filteredRestaurants.map((restaurant) => (
                <div
                  key={restaurant.id}
                  className="restaurant-item"
                  onClick={() => onSelectRestaurant(restaurant)} // 선택된 식당 전달
                >
                  <div className="restaurant-content">
                    <h3 className="restaurant-name">{restaurant.restaurant_name}</h3>
                    <p className="restaurant-address">{restaurant.address}</p>
                    <div className="restaurant-info">
                      <span className="food-type">{restaurant.food_type.join(", ")}</span>
                      <span className="phone-number">전화번호: {restaurant.phone_number}</span>
                    </div>
                  </div>
                </div>
              ))}
              {hasMore && (
                <button className="load-more-button">
                  더 보기
                </button>
              )}
            </>
          ) : (
            !loading && <p className="no-results">검색 결과가 없습니다.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default SearchModal;
