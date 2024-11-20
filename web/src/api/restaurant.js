import customFetch from './apiClient'; // customFetch를 적절히 설정하세요.
// 식당 검색 API 호출 함수
export const searchRestaurants = async (queryParams, userEmail) => {
    try {
        const processedParams = {};
        // 쿼리 파라미터 처리
        for (const key in queryParams) {
            if (Array.isArray(queryParams[key])) {
                if (key === 'searchAfter') {
                    processedParams[key] = JSON.stringify(queryParams[key]);
                } else {
                    processedParams[key] = queryParams[key].join(',');
                }
            } else if (queryParams[key] !== undefined) {
                processedParams[key] = queryParams[key];
            }
        }
        // 쿼리 스트링 생성
        const queryString = new URLSearchParams(processedParams).toString();
        // API 호출
        const response = await customFetch(`/search/restaurants?${queryString}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-User-Email': userEmail, // 사용자 이메일 헤더 추가
            },
        });
        return response; // 성공 시 검색 결과 반환
    } catch (error) {
        console.error('식당 검색 API 에러:', error);
        throw error;
    }
};