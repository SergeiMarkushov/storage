document.addEventListener('DOMContentLoaded', () => {
    loadFileList();
    loadStorageInfo();
    setupModal();
});

let currentVideo = null;
// const host = 'https://192.168.1.47:8443/';
const host = 'https://localhost:8443/';

function loadStorageInfo() {
    fetch(host + 'api/v1/files/storage-info')
        .then(response => response.json())
        .then(data => {
            const storageInfoElement = document.querySelector('.storage-info p');
            const freeSpaceGB = (data.usableSpace / (1024 * 1024 * 1024)).toFixed(2);
            storageInfoElement.textContent = 'Свободное место: ' + freeSpaceGB + 'GB';
        })
        .catch(error => console.error('Error fetching storage info:', error));
}

function loadFileList() {
    fetch(host + 'api/v1/files')
        .then(response => response.json())
        .then(files => {
            const gallery = document.getElementById('gallery');
            gallery.innerHTML = '';
            files.forEach(file => {
                const mediaCard = document.createElement('div');
                mediaCard.classList.add('media-card');

                const fileExtension = file.originFileName.split('.').pop().toLowerCase();
                if (fileExtension === 'mp4' || fileExtension === 'webm') {
                    const video = document.createElement('video');
                    video.src = host + 'api/v1/files/download/' + encodeURIComponent(file.uniqueFileName);
                    console.log(video.src)
                    video.controls = true;
                    video.onclick = () => openModal('video', video.src, video);
                    mediaCard.appendChild(video);
                } else {
                    const img = document.createElement('img');
                    img.src = host + 'api/v1/files/download/' + encodeURIComponent(file.uniqueFileName);
                    console.log(img.src)
                    img.alt = file;
                    img.onclick = () => openModal('image', img.src);
                    mediaCard.appendChild(img);
                }

                gallery.appendChild(mediaCard);
            });
        })
        .catch(error => {
            console.error('Failed to load file list:', error);
        });
}

function setupModal() {
    const modal = document.getElementById('modal');
    const closeBtn = document.querySelector('.close');

    closeBtn.onclick = () => {
        closeModal();
    };

    window.onclick = (event) => {
        if (event.target === modal) {
            closeModal();
        }
    };
}

function openModal(type, src, originalVideo = null) {
    const modal = document.getElementById('modal');
    const modalImage = document.getElementById('modalImage');
    const modalVideo = document.getElementById('modalVideo');

    if (type === 'image') {
        modalImage.src = src;
        modalImage.style.display = 'block';
        modalVideo.style.display = 'none';
    } else if (type === 'video') {
        console.log(originalVideo)
        if (originalVideo) {
            originalVideo.controls = false;
            originalVideo.pause();
            currentVideo = originalVideo;
            // originalVideo = null;
        }
        modalVideo.src = src;
        modalVideo.style.display = 'block';
        modalImage.style.display = 'none';
        originalVideo.pause();
        modalVideo.play(); // Воспроизведение видео в модальном окне
    }

    modal.style.display = 'block';
}

function closeModal() {
    const modal = document.getElementById('modal');
    const modalImage = document.getElementById('modalImage');
    const modalVideo = document.getElementById('modalVideo');

    modal.style.display = 'none';
    modalImage.style.display = 'none';
    modalVideo.style.display = 'none';
    modalImage.src = '';
    modalVideo.src = '';

    if (currentVideo) {
        currentVideo.pause(); // Остановка видео в основном окне
        currentVideo.currentTime = 0;
        currentVideo = null;
    }
}

// function downloadFile(filename) {
//     const url = host + 'api/files/download/' + encodeURIComponent(filename);
//     const downloadProgressBar = document.getElementById('downloadProgressBar');
//     downloadProgressBar.style.display = 'block';
//
//     fetch(url)
//         .then(response => {
//             if (!response.ok) {
//                 throw new Error('Network response was not ok');
//             }
//             const contentLength = response.headers.get('content-length');
//             if (!contentLength) {
//                 throw new Error('Content-Length response header unavailable');
//             }
//             const total = parseInt(contentLength, 10);
//             let loaded = 0;
//
//             const reader = response.body.getReader();
//             const stream = new ReadableStream({
//                 start(controller) {
//                     function push() {
//                         reader.read().then(({done, value}) => {
//                             if (done) {
//                                 controller.close();
//                                 return;
//                             }
//                             loaded += value.length;
//                             downloadProgressBar.value = (loaded / total) * 100;
//                             controller.enqueue(value);
//                             push();
//                         });
//                     }
//
//                     push();
//                 }
//             });
//
//             return new Response(stream);
//         })
//         .then(response => response.blob())
//         .then(blob => {
//             const url = window.URL.createObjectURL(blob);
//             const a = document.createElement('a');
//             a.style.display = 'none';
//             a.href = url;
//             a.download = filename;
//             document.body.appendChild(a);
//             a.click();
//             window.URL.revokeObjectURL(url);
//             downloadProgressBar.style.display = 'none';
//         })
//         .catch(error => {
//             console.error('Download failed:', error);
//             downloadProgressBar.style.display = 'none';
//         });
// }

function deleteFile(filename) {
    fetch(host + 'api/v1/files/delete/' + encodeURIComponent(filename), {
        method: 'DELETE'
    })
        .then(response => {
            if (response.ok) {
                loadFileList();
            } else {
                console.error('Failed to delete file');
            }
        })
        .catch(error => {
            console.error('Failed to delete file:', error);
        });
}
