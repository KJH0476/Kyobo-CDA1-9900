// src/components/Callback.js

import React, { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import queryString from 'query-string';

function Callback() {
    const location = useLocation();

    useEffect(() => {
        const { code, error, error_description } = queryString.parse(location.search);

        if (code) {
            exchangeCodeForTokens(code);
        } else if (error) {
            console.error('로그인 에러:', error, error_description);
            // 에러 처리 로직을 추가하세요.
        }
    }, [location]);

    const exchangeCodeForTokens = async (code) => {
        const clientId = process.env.REACT_APP_COGNITO_CLIENT_ID;
        const redirectUri = process.env.REACT_APP_COGNITO_REDIRECT_URI;
        const tokenUrl = `${process.env.REACT_APP_COGNITO_DOMAIN}/oauth2/token`;

        const params = new URLSearchParams();
        params.append('grant_type', 'authorization_code');
        params.append('client_id', clientId);
        params.append('code', code);
        params.append('redirect_uri', redirectUri);

        try {
            const response = await fetch(tokenUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: params.toString(),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error_description || '토큰 교환 실패');
            }

            const data = await response.json();
            // 액세스 토큰, ID 토큰, 리프레시 토큰을 저장합니다.
            localStorage.setItem('accessToken', data.access_token);
            localStorage.setItem('idToken', data.id_token);
            localStorage.setItem('refreshToken', data.refresh_token);

            // 필요한 경우 사용자 정보를 가져오거나, 메인 페이지로 이동합니다.
            window.location.href = '/';
        } catch (error) {
            console.error('토큰 교환 실패:', error);
            // 에러 처리 로직을 추가하세요.
        }
    };

    return <div>로그인 처리 중...</div>;
}

export default Callback;