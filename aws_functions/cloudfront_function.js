function handler(event) {
    try {
        var request = event.request;
        var headers = request.headers;

        // 리다이렉트를 적용할 경로
        var apiPathPrefix = "/api"; // API 요청 경로

        // 리전별 CloudFront 도메인 정의
        var cloudfrontDomains = {
            kr: 'kr.respa.click',
            us: 'us.respa.click'
        };

        // 기본 리전 설정
        var defaultRegion = 'kr';

        // x-region 헤더에서 'region' 값 추출
        var regionHeader = headers['x-region'] ? headers['x-region'].value.toLowerCase() : null;

        // CloudFront 헤더에서 국가 정보 확인
        var country = headers['cloudfront-viewer-country']
            ? headers['cloudfront-viewer-country'].value.toLowerCase()
            : null;

        // 최종 리전 결정 (x-region이 있으면 우선 사용)
        var finalRegion = regionHeader || country || defaultRegion;

        // API 요청이 아닌 경우 요청 그대로 전달
        if (!request.uri.startsWith(apiPathPrefix)) {
            console.log("Non-API request. Passing through...");
            return request;
        }

        // 타겟 도메인 결정
        var targetDomain = cloudfrontDomains[finalRegion] || cloudfrontDomains[defaultRegion];

        // `/api` 경로 제거
        var strippedUri = request.uri.replace(apiPathPrefix, "");

        // 쿼리 문자열 재구성
        var queryString = '';
        if (request.querystring) {
            var params = [];
            for (var key in request.querystring) {
                if (request.querystring.hasOwnProperty(key)) {
                    var value = request.querystring[key].value;
                    params.push(encodeURIComponent(key) + '=' + encodeURIComponent(value));
                }
            }
            if (params.length > 0) {
                queryString = '?' + params.join('&');
            }
        }

        // 리다이렉트 생성
        return {
            statusCode: 302,
            statusDescription: 'Found',
            headers: {
                location: { value: `https://${targetDomain}${strippedUri}${queryString}` }
            }
        };
    } catch (error) {
        console.error("CloudFront Function Error:", error);
        return event.request;
    }
}