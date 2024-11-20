import customFetch from "./apiClient";


// 예약 정보 확인 API 호출 함수
const BASE_URL = process.env.REACT_APP_BASE_URL;

export const fetchReservations = async (email, jwtToken) => {
  try {
    const response = await customFetch(`${BASE_URL}/reservation/reservations/${email}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${jwtToken}`,
        "X-User-Email": email,
        "Content-Type": "application/json"
      }
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || "예약 정보를 불러오는 데 실패했습니다.");
    }

    const data = await response.json();
    return data; // 성공 시 예약 데이터 배열 반환
  } catch (error) {
    console.error("예약 조회 API 에러:", error);
    throw error;
  }
};

// 예약 취소 API 호출 함수

export const cancelReservation = async (reservationId, jwtToken) => {
  try {
    const response = await customFetch(`${BASE_URL}/reservation/cancel/${reservationId}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${jwtToken}`,
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || "예약 취소에 실패했습니다.");
    }

    return await response.json(); // 성공 응답 반환
  } catch (error) {
    throw error; // 호출한 쪽에서 에러 처리
  }
};

// 예약 생성 API 호출 함수
export const createReservation = async (reservationData, jwtToken) => {
    try {
      const response = await customFetch(`${BASE_URL}/reservation/create`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${jwtToken}`,
          "Content-Type": "application/json",
          "X-User-Email": reservationData.userEmail,
        },
        body: JSON.stringify(reservationData),
      });
  
      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || "예약 생성에 실패했습니다.");
      }
  
      return await response.json(); // 성공 응답 반환
    } catch (error) {
      throw error; // 호출한 쪽에서 에러 처리
    }
  };
  

//예약 대기 API 함수 
export const registerWaitlist = async (waitlistData, jwtToken) => {
  try {
     const response = await customFetch(`${BASE_URL}/reservation/waiting`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${jwtToken}`,
        "Content-Type": "application/json",
        "X-User-Email": waitlistData.userEmail,
      },
      body: JSON.stringify(waitlistData),
    });
  
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || "예약 대기 등록에 실패했습니다.");
    }
  
    return await response.json();
  } catch (error) {
    throw error;
  }
};

// 예약 가능 시간대 조회 API 함수
export const getAvailabilityTimeSlots = async (restaurantId, jwtToken) => {
  try {
    const response = await customFetch(`${BASE_URL}/reservation/availability/${restaurantId}`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${jwtToken}`,
        "X-User-Email": "test@test.com", // 실제 사용자 이메일로 변경
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || "예약 가능 시간대 조회에 실패했습니다.");
    }

    return await response.json();
  } catch (error) {
    throw error;
  }
};