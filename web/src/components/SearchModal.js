import React, { useState } from "react";
import "./SearchModal.css";

const SearchModal = ({ isOpen, onClose, onSelectRestaurant }) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("");

  // 식당 데이터 (한식, 중식, 양식으로만 구분)
  const restaurants = [
    {
      id: 1,
      name: "신촌 파스타",
      address: "서울시 서대문구 신촌로 123",
      type: "양식",
      rating: 4.5,
      reviewCount: 123,
      price: "15,000원",
      lat: 37.556,
      lng: 126.937,
    },
    {
      id: 2,
      name: "홍대 삼겹살",
      address: "서울시 마포구 와우산로 48",
      type: "한식",
      rating: 4.7,
      reviewCount: 543,
      price: "18,000원",
      lat: 37.557,
      lng: 126.925,
    },
    {
      id: 3,
      name: "연남동 짬뽕",
      address: "서울시 마포구 연남로 35",
      type: "중식",
      rating: 4.3,
      reviewCount: 278,
      price: "9,000원",
      lat: 37.56,
      lng: 126.923,
    },
    {
      id: 4,
      name: "이태원 스테이크",
      address: "서울시 용산구 이태원로 154",
      type: "양식",
      rating: 4.6,
      reviewCount: 892,
      price: "35,000원",
      lat: 37.534,
      lng: 126.994,
    },
    {
      id: 5,
      name: "강남 순대국",
      address: "서울시 강남구 강남대로 328",
      type: "한식",
      rating: 4.4,
      reviewCount: 432,
      price: "8,000원",
      lat: 37.498,
      lng: 127.027,
    },
    {
      id: 6,
      name: "명동 짜장면",
      address: "서울시 중구 명동길 54",
      type: "중식",
      rating: 4.2,
      reviewCount: 667,
      price: "7,000원",
      lat: 37.563,
      lng: 126.983,
    },
    {
      id: 7,
      name: "을지로 곱창",
      address: "서울시 중구 을지로 158",
      type: "한식",
      rating: 4.8,
      reviewCount: 892,
      price: "20,000원",
      lat: 37.566,
      lng: 126.985,
    },
    {
      id: 8,
      name: "연남 피자",
      address: "서울시 마포구 동교로 248",
      type: "양식",
      rating: 4.5,
      reviewCount: 445,
      price: "13,000원",
      lat: 37.561,
      lng: 126.924,
    },
    {
      id: 9,
      name: "홍대 감자탕",
      address: "서울시 마포구 와우산로 29",
      type: "한식",
      rating: 4.6,
      reviewCount: 478,
      price: "12,000원",
      lat: 37.554,
      lng: 126.925,
    },
    {
      id: 10,
      name: "이대 마라탕",
      address: "서울시 서대문구 이화여대길 88",
      type: "중식",
      rating: 4.4,
      reviewCount: 234,
      price: "11,000원",
      lat: 37.558,
      lng: 126.946,
    },
  ];

  // 검색어와 카테고리로 필터링
  const filteredRestaurants = restaurants.filter((restaurant) => {
    const matchesSearch =
      restaurant.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      restaurant.address.toLowerCase().includes(searchTerm.toLowerCase());

    if (selectedCategory) {
      return matchesSearch && restaurant.type === selectedCategory;
    }
    return matchesSearch;
  });

  // 카테고리 선택 핸들러
  const handleCategoryClick = (category) => {
    if (selectedCategory === category) {
      setSelectedCategory(""); // 같은 카테고리 다시 클릭하면 필터 해제
    } else {
      setSelectedCategory(category);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="search-modal-left">
      <div className="search-modal-content">
        <div className="search-input-container">
          <span className="search-icon">🔍</span>
          <input
            type="text"
            placeholder="식당이름, 음식종류(한식,중식,양식), 지역을"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
        </div>

        <div className="category-buttons">
          <button
            className={`category-btn ${
              selectedCategory === "한식" ? "active" : ""
            }`}
            onClick={() => handleCategoryClick("한식")}
          >
            <span className="category-icon">🍚</span>
            한식
          </button>
          <button
            className={`category-btn ${
              selectedCategory === "중식" ? "active" : ""
            }`}
            onClick={() => handleCategoryClick("중식")}
          >
            <span className="category-icon">🥢</span>
            중식
          </button>
          <button
            className={`category-btn ${
              selectedCategory === "양식" ? "active" : ""
            }`}
            onClick={() => handleCategoryClick("양식")}
          >
            <span className="category-icon">🍝</span>
            양식
          </button>
        </div>

        <div className="search-results">
          {filteredRestaurants.map((restaurant) => (
            <div
              key={restaurant.id}
              className="restaurant-item"
              onClick={() => onSelectRestaurant(restaurant)}
            >
              <div className="restaurant-content">
                <h3 className="restaurant-name">{restaurant.name}</h3>
                <p className="restaurant-address">{restaurant.address}</p>
                <div className="restaurant-info">
                  <span className="rating">⭐ {restaurant.rating}</span>
                  <span className="review-count">
                    리뷰 {restaurant.reviewCount}
                  </span>
                  <span className="price">평균 {restaurant.price}</span>
                </div>
                <div className="restaurant-type">{restaurant.type}</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default SearchModal;
