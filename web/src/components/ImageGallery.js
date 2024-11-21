import React from 'react';

/*
// S3 이미지 경로 설정
const S3_BASE_URL = 'https://respa.click.s3.amazonaws.com/test1/build/static/media/images/';
/*
const IMAGE_NAMES = [
    'Assorted_Bossam.6b1ea7f0c1246248bda9.jpg',
    'Baggate_image.94920fe1d3060917d0cd.jpg',
    'Bear_soup.b0c52ee0548014b902bc.jpg',
    'Beef_and_Cabbage_Soup.82dc7d563e7f5632dbbaa.jpg',
    'BlackChickenSamgyetang_image.353dade56be0e8586b9.jpg',
    'boiled_pork.151c93ce2cb3d74b825d.jpg',
    'Button_mushroom_cream_soup.19616ce25d7ccc119e74.jpg',
    'cabbage_pancake.0690a91214f97bcb1ab4.jpg',
    'CALIFORNIA_ST_LASAGNA.9c96ab65c2113a5c9be4.jpg',
    'Cheonggukjang.9fe5ed7c0fb37353194f.jpg',
    'Cold_Cold_Jjamppong.73fb66a6d44227ac2197.jpg',
    'Corn_Cream_Gnocchi.e2d342fd261cd4b1af36.jpg',
    'corn_pizza.01a1532e47f24e14d312.jpg',
    'Corn_Tower_Pizza_CEO.b1bf3d2ee99f7686f22c.jpg',
    'Ganjjajang2.f70fb75778318f86ec70.jpg',
    'Garlic_squid_oil_pasta.e05218d7a54931ded1f1.jpg',
    'green_onion_ pancake.2e6c05fdecf0bdac0bbc.jpg',
    'House_Steak.c3c3420638e89992ab49.jpg',
    'JAMBON_BEURRE_PASTA.dcd4d927885845068327.jpg',
    'Japchae_Fried_Rice.6ce03c419b991c464fcb.jpg',
    'Jeongabok.4c75fe9a053cdca197e6.jpg',
    'Jeyuk_Ssambap.52a66dcf4f48d808bf23.jpg',
    'Jjajangmyeon.337fda9ef24f52eb658f.jpg',
    'Jjambbong.b8dd1ecdb740030139dc.jpg',
    'Lagupasta_image.48e8ab7ae935f590f544.jpg',
    'Lasagna_image.2de8bf3e04299181272c.jpg',
    'Marbled_Jjamppong.dce9543990ab3f50e955.jpg',
    'Nanjawans.dcc269d7cc266055b363.jpg',
    'Neungdong Yukhoe.22e7736c15bb3de1050c.jpg',
    'Neungdong_water_parsley_gomtang.1d326fb83c754230227d.jpg',
    'Neungdong_Yukhoe_Bibimbap.f1728ba6a13b5743f863.jpg',
    'Neungdong_Yukhoe.22e7736c15bb3de1050c.jpg',
    'Oyster_Jjamppong.d4e4b863d08195846057.jpg',
    'Pepperoni_Pizza.c61fac1cc3ffa96a6c53.jpg',
    'Plane_Pork_Ssambap.063e77158d5d1c2da3cd.jpg',
    'PORCINI_GNOCCHI.e755f6940121ee3641ee.jpg',
    'Pork_belly_rose_pasta.c9f72466bfbdb769a236.jpg',
    'potato_pancake.e740d01506c1110ae3d2.jpg',
    'respa-kawaii-logo4.11bd02e43ced9ff061e0.png',
    'Richard_Pork_Rip.4af6545cb3b1be4bb18d.jpg',
    'Salade_image.09c12c7c6e45e144fc9c.jpg',
    'Samgyetang_image.620d88a474e74ce22826.jpg',
    'Samseon_Jjambbong2.c0995c5b41638566ee14.jpg',
    'Samseon_Seafood_Jajang_Dosakmyeon.2d72a319d181a30db529.jpg',
    'Samseonganjjajang.d4fb7038673d36a1d093.jpg',
    'Seafood_and_green_onion_pancake.de519a1f6f92b5e97f08.jpg',
    'Seasonal_raw_fish_jelly_roll.c8ad6bd6ef051cc298d8.jpg',
    'Signature_Steak.ed3c2bbbbb8be8f80795.jpg',
    'Son_Guk_Poetry.291bcb16bcef7a83d6a6.jpg',
    'Straw_Bulhangjeongsal.e40b88c81639d7f24ede.jpg',
    'Strong_soybean_paste_ssambap.e17e1f2dad8090915fad.jpg',
    'sujebi.a588e69c9319580eb598.jpg',
    'Sundae_image.dc852cc188249a4c785f.jpg',
    'taste_steak.b8a9b05626efe42c456f.jpg',
    'Tornado_Omelet_Rice.f96d0a7efe189e7d3dc6.jpg',
    'Tteokbokki_image.eff477e9b81ce739130d.jpg',
    'Woodae_Galbi.f7099f4cbdd9ebeb6622.jpg',
    'Yukmi_Jjajang.ff069b1b9d0d12a5e170.jpg',
    'Yukmi-ganjjajang.cae4016a22e3caed1923.jpg',
    'Yuseul_Stir-fried_Jajangdosakmyeon.2c7a0ab0d0b87fa0f326.jpg',

    
    // 추가 이미지 이름을 여기에 추가
];
*/


/*
const images = IMAGE_NAMES.map((name) => ({
    src: `${S3_BASE_URL}${name}`,
    alt: name.split('.')[0].replace(/_/g, ' '),
}));
*/
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
