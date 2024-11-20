import React, { useState } from "react";
import "./SearchModal.css";

const SearchModal = ({ isOpen, onClose, onSelectRestaurant, userEmail }) => {
  const [searchTerm, setSearchTerm] = useState(""); // 식당 이름 검색어
  const [locationTerm, setLocationTerm] = useState(""); // 지역 검색어
  const [selectedCategories, setSelectedCategories] = useState([]); // 선택된 카테고리
  const [filteredRestaurants, setFilteredRestaurants] = useState([]); // 검색 결과
  const [loading, setLoading] = useState(false); // 로딩 상태
  const [error, setError] = useState(null); // 에러 상태

  const BASE_URL = process.env.REACT_APP_BASE_URL;

  // 검색 버튼 클릭 시 Mock 데이터 필터링
  const handleSearch = async () => {
    setLoading(true);
    setError(null);
    try {
      const queryParams = new URLSearchParams({
        restaurant_name: searchTerm || "",
        address: locationTerm || "",
        food_type: selectedCategories.length > 0 ? selectedCategories.join(",") : "",
      }).toString();
      const response = await fetch(`${BASE_URL}/search/restaurants?${queryParams}`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "X-User-Email": userEmail,
        },
      });
      if (!response.ok) {
        throw new Error("검색에 실패했습니다.");
      }
      const data = await response.json();
      if (data.statusCode === 200) {
        setFilteredRestaurants(data.restaurants);
      } else {
        setFilteredRestaurants([]);
        setError(data.message || "검색에 실패했습니다.");
      }

    } catch (err) {
      console.error("검색 중 오류 발생:", err);
      setError("검색에 실패했습니다. 다시 시도해주세요.");
    } finally {
      setLoading(false);
    }
  };
  // 카테고리 버튼 클릭 시 선택/해제
  const handleCategoryClick = (category) => {
    if (selectedCategories.includes(category)) {
      setSelectedCategories(selectedCategories.filter((c) => c !== category));
    } else {
      setSelectedCategories([...selectedCategories, category]);
    }
  };
  if (!isOpen) return null; // 모달이 닫힌 경우 null 반환
  return (
      <div className="search-modal-left">
        <div className="search-modal-content">
          {/* 검색 입력 */}
          <div className="search-input-container">
            <span className="search-icon">🔍</span>
            <input
                type="text"
                placeholder="식당 이름을 입력하세요"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="search-input"
            />
          </div>
          <div className="location-input-container">
            <span className="search-icon">🔍</span>
            <input
                type="text"
                placeholder="지역 이름을 입력하세요 (예: 강남, 홍대)"
                value={locationTerm}
                onChange={(e) => setLocationTerm(e.target.value)}
                className="search-input"
            />
          </div>
          {/* 카테고리 버튼 */}
          <div className="category-buttons">
            <button
                className={`category-btn ${selectedCategories.includes("한식") ? "active" : ""}`}
                onClick={() => handleCategoryClick("한식")}
            >
              <span className="category-icon">🍚</span>
              한식
            </button>
            <button
                className={`category-btn ${selectedCategories.includes("중식") ? "active" : ""}`}
                onClick={() => handleCategoryClick("중식")}
            >
              <span className="category-icon">🥢</span>
              중식
            </button>
            <button
                className={`category-btn ${selectedCategories.includes("양식") ? "active" : ""}`}
                onClick={() => handleCategoryClick("양식")}
            >
              <span className="category-icon">🍝</span>
              양식
            </button>
          </div>
          <div className="search-button-container">
            <button className="search-button" onClick={handleSearch}>
              검색
            </button>
          </div>
          {/* 로딩 상태 */}
          {loading && <p className="loading-message">검색 중입니다...</p>}
          {/* 에러 메시지 */}
          {error && <p className="error-message">{error}</p>}
          {/* 검색 결과 */}
          <div className="search-results">
            {filteredRestaurants.length > 0 ? (
                filteredRestaurants.map((restaurant) => (
                    <div
                        key={restaurant.id}
                        className="restaurant-item"
                        onClick={() => onSelectRestaurant(restaurant)}
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
                ))
            ) : (
                !loading && <p className="no-results">검색 결과가 없습니다.</p>
            )}
          </div>
        </div>
      </div>
  );
};
export default SearchModal;