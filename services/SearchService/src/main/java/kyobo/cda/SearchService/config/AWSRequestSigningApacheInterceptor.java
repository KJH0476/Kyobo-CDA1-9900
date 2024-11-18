package kyobo.cda.SearchService.config;

import com.amazonaws.ReadLimitInfo;
import com.amazonaws.SignableRequest;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.http.HttpMethodName;
import org.apache.http.*;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AWSRequestSigningApacheInterceptor implements HttpRequestInterceptor {
    private final AWS4Signer signer;
    private final AWSCredentialsProvider awsCredentialsProvider;

    public AWSRequestSigningApacheInterceptor(String serviceName, String region, AWSCredentialsProvider awsCredentialsProvider) {
        this.signer = new AWS4Signer();
        this.signer.setServiceName(serviceName);
        this.signer.setRegionName(region);
        this.awsCredentialsProvider = awsCredentialsProvider;
    }

    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        // 요청을 래핑합니다.
        HttpRequestWrapper requestWrapper = HttpRequestWrapper.wrap(request);

        // 컨텍스트에서 타겟 호스트를 가져옵니다.
        HttpHost targetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
        if (targetHost == null) {
            throw new HttpException("Target host not set in the context");
        }

        // 요청 URI를 가져와서 전체 URI를 구성합니다.
        String uri = requestWrapper.getURI().toString();
        if (!uri.startsWith("http://") && !uri.startsWith("https://")) {
            uri = targetHost.toURI() + uri;
        }

        // 서명 가능한 요청을 생성합니다.
        SignableRequest<?> signableRequest = new SignableApacheRequest(requestWrapper, URI.create(uri));

        // 요청에 서명을 적용합니다.
        signer.sign(signableRequest, awsCredentialsProvider.getCredentials());

        // 서명된 헤더를 원본 요청에 추가합니다.
        for (Map.Entry<String, String> header : signableRequest.getHeaders().entrySet()) {
            request.setHeader(header.getKey(), header.getValue());
        }
    }

    private static class SignableApacheRequest implements SignableRequest<HttpRequest> {
        private final HttpRequestWrapper request;
        private final URI uri;
        private final Map<String, String> headers = new HashMap<>();
        private final Map<String, List<String>> parameters = new HashMap<>();
        private InputStream content;

        public SignableApacheRequest(HttpRequestWrapper request, URI uri) {
            this.request = request;
            this.uri = uri;
            this.content = null;
        }

        @Override
        public void addHeader(String name, String value) {
            request.addHeader(name, value);
            headers.put(name, value);
        }

        @Override
        public Map<String, String> getHeaders() {
            Map<String, String> allHeaders = new HashMap<>();
            for (Header header : request.getAllHeaders()) {
                allHeaders.put(header.getName(), header.getValue());
            }
            return allHeaders;
        }

        @Override
        public String getResourcePath() {
            return uri.getPath();
        }

        @Override
        public Map<String, List<String>> getParameters() {
            // 쿼리 파라미터를 파싱합니다.
            Map<String, List<String>> params = new HashMap<>();
            String query = uri.getQuery();
            if (query != null && !query.isEmpty()) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    String key = idx > 0 ? pair.substring(0, idx) : pair;
                    String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : "";
                    params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
                }
            }
            return params;
        }

        @Override
        public void addParameter(String name, String value) {
            parameters.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        }

        public void setParameters(Map<String, List<String>> parameters) {
            this.parameters.clear();
            this.parameters.putAll(parameters);
        }

        @Override
        public URI getEndpoint() {
            return URI.create(uri.getScheme() + "://" + uri.getHost());
        }

        @Override
        public HttpMethodName getHttpMethod() {
            return HttpMethodName.fromValue(request.getMethod());
        }

        @Override
        public InputStream getContent() {
            if (content != null) {
                return content;
            } else if (request instanceof HttpEntityEnclosingRequest) {
                try {
                    HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                    if (entity == null) {
                        return new ByteArrayInputStream(new byte[0]);
                    }
                    if (!entity.isRepeatable() || entity.getContentLength() < 0) {
                        entity = new BufferedHttpEntity(entity);
                        ((HttpEntityEnclosingRequest) request).setEntity(entity);
                    }
                    return entity.getContent();
                } catch (IOException e) {
                    return new ByteArrayInputStream(new byte[0]);
                }
            } else {
                return new ByteArrayInputStream(new byte[0]);
            }
        }

        @Override
        public InputStream getContentUnwrapped() {
            return null;
        }

        @Override
        public void setContent(InputStream content) {
            this.content = content;
        }

        @Override
        public ReadLimitInfo getReadLimitInfo() {
            return () -> 0;
        }

        @Override
        public int getTimeOffset() {
            return 0;
        }

        @Override
        public HttpRequest getOriginalRequestObject() {
            return request;
        }
    }
}
