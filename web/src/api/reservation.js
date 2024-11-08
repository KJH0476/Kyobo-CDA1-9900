// 예약 관련 API


// 예약 정보 확인 API 호출 함수
const BASE_URL = process.env.REACT_APP_BASE_URL || 'http://localhost:8000';

/**
 * 예약 정보 조회
 * @param {string} email - 예약 정보를 조회할 사용자의 이메일
 * @param {string} jwtToken - 사용자의 JWT 액세스 토큰
 * @returns {Promise<Object[]>} - 예약 정보 배열
 */
export const fetchReservations = async (email, jwtToken) => {
  try {
    const response = await fetch(`${BASE_URL}/reservation/reservations/${email}`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${jwtToken}`,
        "X-User-Email": email,
      },
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || "예약 정보를 불러오는 데 실패했습니다.");
    }

    return await response.json(); // 예약 정보 반환
  } catch (error) {
    throw error; // 호출한 쪽에서 에러 처리
  }
};




// 예약 취소 API 호출 함수

/**
 * 예약 취소
 * @param {string} reservationId - 취소할 예약 ID
 * @param {string} jwtToken - 사용자의 JWT 액세스 토큰
 * @param {Object} cancellationData - 취소 요청에 필요한 데이터
 * @returns {Promise<Object>} - API 응답 객체
 */
export const cancelReservation = async (reservationId, jwtToken, cancellationData) => {
  try {
    const response = await fetch(`${BASE_URL}/reservation/${reservationId}/cancel`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${jwtToken}`,
        "Content-Type": "application/json",
        "X-User-Email": cancellationData.userEmail,
      },
      body: JSON.stringify(cancellationData),
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



// 예약 생성 A
/**
 * 예약 생성
 * @param {Object} reservationData - 예약 생성에 필요한 데이터
 * @param {string} jwtToken - 사용자의 JWT 액세스 토큰
 * @returns {Promise<Object>} - API 응답 객체
 */
export const createReservation = async (reservationData, jwtToken) => {
    try {
      const response = await fetch(`${BASE_URL}/reservation/create`, {
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
  