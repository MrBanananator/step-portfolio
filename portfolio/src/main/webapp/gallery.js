var imgIndex = 0;
var imgDir = 'images/Me/';
var imgArray = new Array(
    'PICS OFF STICK (106).JPG',
    'PICS OFF STICK (134).jpg',
    'PICS OFF STICK (155).jpg',
    'PICS OFF STICK (179).jpg',
    'PICS OFF STICK (218).jpg',
    'PICS OFF STICK (646).jpg',
    'PICS OFF STICK (910).jpg',
    'PICS OFF STICK (927).jpg',
    'PICS OFF STICK (927).jpg',
    'PICS OFF STICK (960).jpg',
    'PICS OFF STICK (966).jpg'
);

function randomImage() {
    var theImage = document.getElementById('myimage');

    if (imgIndex < imgArray.length - 1) {
        imgIndex += 1;
    } else {
        imgIndex = 0;
    }

    var imgPath = imgDir + imgArray[imgIndex];
    
    theImage.src = imgPath;
    theImage.alt = imgArray[imgIndex];
}
		