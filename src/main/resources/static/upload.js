document.addEventListener('DOMContentLoaded', () => {
    loadFileList();
});

// const host = 'https://192.168.1.47:8443/';
const host = 'https://localhost:8443/';

function uploadFiles() {
    const input = document.getElementById('fileInput');
    const formData = new FormData();
    const uploadProgressBar = document.getElementById('uploadProgressBar');
    uploadProgressBar.style.display = 'block';

    for (let i = 0; i < input.files.length; i++) {
        formData.append('files', input.files[i]);
    }

    const xhr = new XMLHttpRequest();
    xhr.open('POST', host + 'api/v1/files/upload', true);

    xhr.upload.onprogress = function (event) {
        if (event.lengthComputable) {
            const percentComplete = (event.loaded / event.total) * 100;
            uploadProgressBar.value = percentComplete;
        }
    };

    xhr.onload = function () {
        if (xhr.status === 200) {
            document.getElementById('uploadStatus').textContent = xhr.responseText;
            loadFileList();
        } else {
            document.getElementById('uploadStatus').textContent = xhr.responseText;
        }
        uploadProgressBar.style.display = 'none';
    };

    xhr.send(formData);
}

function loadFileList() {
    fetch(host + 'api/v1/files/documents')
        .then(response => response.json())
        .then(files => {
            const fileList = document.getElementById('fileList');
            fileList.innerHTML = '';
            files.forEach(file => {
                const listItem = document.createElement('li');
                const link = document.createElement('a');
                link.href = '#';
                link.textContent = file.originFileName;
                link.onclick = () => {
                    downloadFile(file.uniqueFileName, file.originFileName);
                };

                const deleteButton = document.createElement('button');
                deleteButton.textContent = 'Delete';
                deleteButton.onclick = () => deleteFile(file.uniqueFileName);

                listItem.appendChild(link);
                listItem.appendChild(deleteButton);
                fileList.appendChild(listItem);
            });
        })
        .catch(error => {
            console.error('Failed to load file list:', error);
        });
}

function downloadFile(uniqueFileName, originFileName) {
    const url = host + 'api/v1/files/download/' + uniqueFileName;
    const downloadProgressBar = document.getElementById('downloadProgressBar');
    downloadProgressBar.style.display = 'block';

    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const contentLength = response.headers.get('content-length');
            if (!contentLength) {
                throw new Error('Content-Length response header unavailable');
            }
            const total = parseInt(contentLength, 10);
            let loaded = 0;

            const reader = response.body.getReader();
            const stream = new ReadableStream({
                start(controller) {
                    function push() {
                        reader.read().then(({done, value}) => {
                            if (done) {
                                controller.close();
                                return;
                            }
                            loaded += value.length;
                            downloadProgressBar.value = (loaded / total) * 100;
                            controller.enqueue(value);
                            push();
                        });
                    }

                    push();
                }
            });

            return new Response(stream);
        })
        .then(response => response.blob())
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            a.download = originFileName;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            downloadProgressBar.style.display = 'none';
        })
        .catch(error => {
            console.error('Download failed:', error);
            downloadProgressBar.style.display = 'none';
        });
}

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