import React from 'react';

// src/img 폴더 내 모든 이미지 동적으로 가져오기
const importAll = (requireContext) =>
    requireContext.keys().map((key) => ({
        src: requireContext(key), // 파일 경로
        alt: key.replace('./', '').replace(/\.[^/.]+$/, ''), // 파일명에서 확장자 제거
    }));

const images = importAll(require.context('../img', false, /\.(png|jpe?g|svg)$/));

function ImageGallery() {
    return (
        <div style={{ textAlign: 'center' }}>
            <h1>Image Gallery</h1>
            <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center' }}>
                {images.map((image, index) => (
                    <img
                        key={index}
                        src={image.src}
                        alt={image.alt}
                        style={{
                            width: '300px',
                            height: '200px',
                            objectFit: 'cover',
                            margin: '10px',
                            borderRadius: '8px',
                            boxShadow: '0 4px 8px rgba(0, 0, 0, 0.2)',
                        }}
                    />
                ))}
            </div>
        </div>
    );
}

export default ImageGallery;
