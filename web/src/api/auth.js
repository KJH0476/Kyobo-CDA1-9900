const BASE_URL = process.env.REACT_APP_BASE_URL;

export const login = async (email, password) => {
  try {
    console.log('로그인 시도:', { email, password }); // 요청 확인

    const response = await fetch(`${BASE_URL}/pass/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': '*'  // CORS 헤더 추가
      },
      credentials: 'include',  // 쿠키 포함
      body: JSON.stringify({ email, password })
    });

    console.log('서버 응답:', response); // 응답 확인

    const data = await response.json();
    console.log('응답 데이터:', data); // 데이터 확인

    if (response.ok) {
      return { success: true, data };
    } else {
      return { 
        success: false, 
        message: data.message || "네트워크 오류가 발생했습니다." 
      };
    }
  } catch (error) {
    console.error('API 에러:', error);
    return { 
      success: false, 
      message: "서버와의 통신 중 오류가 발생했습니다." 
    };
  }
};

// src/api/auth.js

export const signup = async (email, username, password) => {
  try {
    const response = await fetch(`${BASE_URL}/pass/signup`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-User-Email': email,
      },
      body: JSON.stringify({ email, username, password }),
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || '회원가입에 실패했습니다.');
    }

    return data; // 성공 시 { statusCode: 201, message: "success signup", userDto: { ... } } 형태로 반환
  } catch (error) {
    console.error('회원가입 에러:', error);
    throw error;
  }
};

