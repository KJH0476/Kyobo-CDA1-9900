import React from 'react';

// src/img 폴더 내 모든 이미지 동적으로 가져오기
const importAll = (requireContext) =>
    requireContext.keys().map((key) => ({
        src: requireContext(key), // 파일 경로가져오기
        alt: key.replace('./', '').replace(/\.[^/.]+$/, ''), // 파일명에서 확장자를 제거하고 alt 속성으로 사용합니다.
    }));

// 특정 폴더 내의 이미지를 자동으로 가져옵니다.
// `require.context`를 사용하여 '../img' 폴더 내 모든 png, jpg, jpeg, svg 파일을 동적으로 가져옵니다.
const images = importAll(require.context('../img', false, /\.(png|jpe?g|svg)$/));

function ImageGallery() {
    return (
        <div style={{ textAlign: 'center' }}> {/* 중앙 정렬 스타일 적용 */}
            <h1>Image Gallery</h1> {/* 제목 표시 */}
            <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center' }}>
                {/* 가져온 이미지 리스트를 반복하여 출력 */}
                {images.map((image, index) => (
                    <img
                        key={index} // 각 이미지 요소에 고유한 키를 설정
                        src={image.src} // 이미지 파일 경로
                        alt={image.alt} // 이미지 설명 (파일명에서 확장자 제거)
                        style={{
                            width: '300px', // 이미지 너비를 300px로 고정
                            height: '200px', // 이미지 높이를 200px로 고정
                            objectFit: 'cover', // 이미지를 영역에 맞게 채우되 비율 유지
                            margin: '10px', // 이미지 간의 여백
                            borderRadius: '8px', // 이미지 모서리를 둥글게 처리
                            boxShadow: '0 4px 8px rgba(0, 0, 0, 0.2)', // 그림자 스타일로 입체감 추가
                        }}
                    />
                ))}
            </div>
        </div>
    );
}

export default ImageGallery;
