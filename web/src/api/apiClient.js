//src/api/apiClient.js
const BASE_URL = process.env.REACT_APP_BASE_URL;
// 토큰을 가져오는 함수
function getAccessToken() {
    return localStorage.getItem('accessToken');
}
// 토큰을 저장하는 함수
function setAccessToken(token) {
    localStorage.setItem('accessToken', token);
}
// 토큰을 제거하는 함수
function removeTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('currentUser');
}
// 커스텀 fetch 함수
export default async function customFetch(endpoint, options = {}, retryCount = 3) {
    let response = null;
    let headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    try {
        response = await fetch(endpoint, options);
        const clonedResponse = response.clone(); // Response를 복사

        const data = await clonedResponse.json();

        if (data.statusCode === undefined) {
            data.statusCode = response.status;
        }

        // **상황 1: 액세스 토큰 만료 & 리프레시 토큰 유효**
        if (data.statusCode === 200 && data.message === 'success reissued token' && data.accessToken) {
            setAccessToken(data.accessToken);
            if (retryCount < 1) {
                headers['Authorization'] = `Bearer ${data.accessToken}`;
                options.headers = headers;
                return await customFetch(endpoint, options, retryCount + 1);
            } else {
                return Promise.reject(new Error('Retry limit exceeded'));
            }
        }

        // **상황 2: 리프레시 토큰도 만료된 경우**
        if (data.statusCode === 401 && (data.message === 'expired refresh token' || data.message === 'invalid token')) {
            removeTokens();
            window.location.href = '/login';
            return Promise.reject(data);
        }

        // **상황 3: 액세스 토큰이 잘못된 경우**
        if (data.statusCode === 401 && data.message === 'invalid token') {
            removeTokens();
            window.location.href = '/login';
            return Promise.reject(new Error('Refresh token expired'));
        }

        return response;
    } catch (error) {
        return Promise.reject(error);
    }
}